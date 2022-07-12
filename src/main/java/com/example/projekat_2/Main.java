package com.example.projekat_2;

import com.example.projekat_2.Utils.Try;
import com.example.projekat_2.arena.*;
import com.example.projekat_2.timer.Timer;
import javafx.application.Application;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;

public class Main extends Application implements EventHandler {
    private static final double WINDOW_WIDTH = 800;
    private static final double WINDOW_HEIGHT = 800;

    private static final double PODIUM_WIDTH = 2000;
    private static final double PODIUM_HEIGHT = 10;
    private static final double PODIUM_DEPTH = 2000;

    private static final double CAMERA_FAR_CLIP = 100000;
    private static final double CAMERA_Z = -5000;
    private static final double CAMERA_X_ANGLE = -45;

    private static final double BALL_RADIUS = 50;

    private static final double DAMP = 0.999;

    private static final double MAX_ANGLE_OFFSET = 30;
    private static final double MAX_ACCELERATION = 400;

    private static final int NUMBER_OF_HOLES = 4;
    private static final double HOLE_RADIUS = 2 * Main.BALL_RADIUS;
    private static final double HOLE_HEIGHT = PODIUM_HEIGHT;

    public static double REAL_MAX_ACCELERATION = MAX_ACCELERATION;

    private Group main_root;
    private Group root;
    private Group root_utils;
    private Group root_map;
    private Group root_endgame;
    private Group root_time;

    private Ball ball;
    private Arena arena;
    private Hole hole;

    private Scene main_scene;
    private SubScene game_scene;
    private Camera top_view;
    private boolean was_topview;

    private Camera camera;

    private Translate zoom;
    private Rotate sphereX;
    private Rotate sphereY;
    private Translate pan;
    private Rotate tilt;

    private Fence fences[];

    private Line speedLine;

    private Try tries[];
    private int numberOfTries;

    private Text pointsText;
    private int points;

    private ArrayList<Token> myTokens;

    private Lamp sky_lamp;
    private PointLight light_source;

    private Obstacle obstacles[];

    PhongMaterial ballMaterial;
    Translate ballPosition;

    String curTerrain;

    private Bad_Hole bad_holes[];

    private Elastic_Obstacle elastic_obstacles[];

    public static final double MAX_TIME = 200;
    public static double timePassed = MAX_TIME;
    private Text timeText;
    private boolean startTimer;

    @Override
    public void handle(Event event) {
        if (event instanceof KeyEvent) {
            handleKeyEvent((KeyEvent) event);
        } else if (event instanceof ScrollEvent) {
            handleScrollEvent((ScrollEvent) event);
        } else if (event instanceof MouseEvent) {
            handleMouseEvent((MouseEvent) event);
        }
    }


    private void handleKeyEvent(KeyEvent event) {
        if (event.getCode().equals(KeyCode.DIGIT2)) {
            this.game_scene.setCamera(top_view);
            was_topview = true;
        } else if (event.getCode().equals(KeyCode.DIGIT1)) {
            this.game_scene.setCamera(camera);
            was_topview = false;
        } else if (event.getCode().equals(KeyCode.DIGIT0) && event.getEventType().equals(KeyEvent.KEY_PRESSED)) {
            if (this.sky_lamp.isLightOn()) {
                this.sky_lamp.light_off();
                this.arena.getChildren().remove(this.light_source);
            } else {
                this.sky_lamp.light_on();
                this.light_source = new PointLight(Color.WHITE);
                this.light_source.getTransforms().addAll(new Translate(0, -1000, 0));
                this.arena.getChildren().addAll(this.light_source);
            }
        }
    }

    private void handleScrollEvent(ScrollEvent event) {
        if (event.getDeltaY() > 0) {
            if (this.zoom.getZ() < -1000) this.zoom.setZ(this.zoom.getZ() + 20);
        } else {
            if (this.zoom.getZ() > CAMERA_Z) this.zoom.setZ(this.zoom.getZ() - 20);
        }
    }

    private double prevX;
    private double prevY;

    private void handleMouseEvent(MouseEvent event) {
        if (event.getEventType().equals(MouseEvent.MOUSE_DRAGGED)) {
            if (this.prevX == 0) {
                this.prevX = event.getSceneX();
            }

            if (this.prevY == 0) {
                this.prevY = event.getSceneY();
            }

            double x = event.getSceneX();
            double y = event.getSceneY();

            double dx = x - this.prevX;
            double dy = y - this.prevY;

            this.prevX = x;
            this.prevY = y;

            int signX = dx > 0 ? 1 : -1;
            int signY = dy > 0 ? 1 : -1;


            if (event.isPrimaryButtonDown()) {
                this.sphereY.setAngle(this.sphereY.getAngle() + signX * 0.3);
                if (this.sphereX.getAngle() - signY * 0.3 < 0 && this.sphereX.getAngle() - signY * 0.3 > -90)
                    this.sphereX.setAngle(this.sphereX.getAngle() - signY * 0.3);
            }
        }
    }

    public void addFences() {
        Translate pos1 = new Translate(0, -45, 990);
        Translate pos2 = new Translate(0, -45, -990);
        Translate pos3 = new Translate(990, -45, 0);
        Translate pos4 = new Translate(-990, -45, 0);
        Fence fence1 = new Fence(700, 80, 10, new PhongMaterial(Color.DARKRED), pos1, "up/down");
        Fence fence2 = new Fence(700, 80, 10, new PhongMaterial(Color.DARKRED), pos2, "up/down");
        Fence fence3 = new Fence(10, 80, 700, new PhongMaterial(Color.DARKRED), pos3, "left/right");
        Fence fence4 = new Fence(10, 80, 700, new PhongMaterial(Color.DARKRED), pos4, "left/right");

        this.fences = new Fence[]{fence1, fence2, fence3, fence4};
    }

    private void addMap() {
        root_map = new Group();

        SubScene subScene = new SubScene(root_map, 150, 150, true, SceneAntialiasing.BALANCED);
        subScene.getTransforms().addAll(new Translate(0, WINDOW_HEIGHT - 150));
        subScene.setDepthTest(DepthTest.DISABLE);
        subScene.setFill(Color.DARKGREEN);

        AddMapBorders(subScene);

        speedLine = new Line();
        speedLine.setStroke(Color.RED);
        speedLine.getTransforms().addAll(new Translate(subScene.getWidth() / 2, subScene.getHeight() / 2));
        root_map.getChildren().addAll(speedLine);

        this.main_root.getChildren().addAll(subScene);
    }

    private void AddMapBorders(SubScene subScene) {
        Rectangle mapBorderLeft = new Rectangle(3, subScene.getHeight());
        mapBorderLeft.setFill(Color.RED);

        Rectangle mapBorderRight = new Rectangle(3, subScene.getHeight());
        mapBorderRight.getTransforms().addAll(new Translate(subScene.getWidth() - 3, 0));
        mapBorderRight.setFill(Color.RED);

        Rectangle mapBorderUp = new Rectangle(subScene.getWidth(), 3);
        mapBorderUp.setFill(Color.RED);

        Rectangle mapBorderDown = new Rectangle(subScene.getWidth(), 3);
        mapBorderDown.getTransforms().addAll(new Translate(0, subScene.getHeight() - 3));
        mapBorderDown.setFill(Color.RED);

        root_map.getChildren().addAll(mapBorderLeft, mapBorderRight, mapBorderUp, mapBorderDown);
    }

    private void addTries() {

        root_utils = new Group();

        SubScene subScene = new SubScene(root_utils, WINDOW_WIDTH, 30, true, SceneAntialiasing.BALANCED);
        subScene.setDepthTest(DepthTest.DISABLE);

        final double tryRad = 8;

        Try try1 = new Try(tryRad, new Translate(20, 20));
        Try try2 = new Try(tryRad, new Translate(40, 20));
        Try try3 = new Try(tryRad, new Translate(60, 20));
        Try try4 = new Try(tryRad, new Translate(80, 20));
        Try try5 = new Try(tryRad, new Translate(100, 20));

        this.tries = new Try[]{try1, try2, try3, try4, try5};
        this.root_utils.getChildren().addAll(tries);
        this.root_utils.getChildren().addAll(addPoints(0));

        this.main_root.getChildren().addAll(subScene);
    }

    private void addEndGameScreen() {
        root_endgame = new Group();
        SubScene subScene = new SubScene(root_endgame, WINDOW_WIDTH, WINDOW_HEIGHT, true, SceneAntialiasing.BALANCED);
        subScene.setDepthTest(DepthTest.DISABLE);

        Text endgame_text = new Text("KRAJ IGRE");
        endgame_text.setFill(Color.WHITESMOKE);
        endgame_text.setStyle("-fx-font:35 ariel;");
        endgame_text.getTransforms().addAll(
                new Translate(WINDOW_WIDTH / 2 - WINDOW_WIDTH / 10, WINDOW_HEIGHT / 2)
        );

        this.root_endgame.getChildren().addAll(endgame_text);
        this.main_root.getChildren().addAll(subScene);

    }

    private Text addPoints(int points) {
        pointsText = new Text("" + points);
        pointsText.setFill(Color.RED);
        pointsText.setStyle("-fx-font:24 ariel;");
        pointsText.getTransforms().addAll(new Translate(WINDOW_WIDTH * 57 / 60, 25));
        return pointsText;
    }

    private void addTokens(Translate pos1, Translate pos2, Translate pos3, Translate pos4) {
        Rotate rotate = new Rotate(-90, Rotate.X_AXIS);


        Token token1 = new Token(40, 10, new PhongMaterial(Color.DARKGOLDENROD), pos1, rotate);


        Token token2 = new Token(40, 10, new PhongMaterial(Color.DARKGOLDENROD), pos2, rotate);


        Token token3 = new Token(40, 10, new PhongMaterial(Color.DARKGOLDENROD), pos3, rotate);


        Token token4 = new Token(40, 10, new PhongMaterial(Color.DARKGOLDENROD), pos4, rotate);

        myTokens.add(token1);
        myTokens.add(token2);
        myTokens.add(token3);
        myTokens.add(token4);
    }

    public void addLamp() {
        Translate position = new Translate(0, -1000, 0);
        this.sky_lamp = new Lamp(100, 100, 100, new PhongMaterial(Color.GRAY), position);
    }

    public void addObstacles(Translate pos1, Translate pos2, Translate pos3, Translate pos4) {
        Image obstaclePattern = new Image("obstacle.jpg");
        PhongMaterial mat = new PhongMaterial();
        mat.setDiffuseMap(obstaclePattern);


        Obstacle obstacle1 = new Obstacle(50, 200, mat, pos1);


        Obstacle obstacle2 = new Obstacle(50, 200, mat, pos2);


        Obstacle obstacle3 = new Obstacle(50, 200, mat, pos3);


        Obstacle obstacle4 = new Obstacle(50, 200, mat, pos4);

        this.obstacles = new Obstacle[]{obstacle1, obstacle2, obstacle3, obstacle4};

    }

    public void addBadHoles(Translate pos1, Translate pos2, Translate pos3) {
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseMap(new Image("badhole_texture.jpg"));

        Bad_Hole bad_hole1 = new Bad_Hole(Main.HOLE_RADIUS, Main.HOLE_HEIGHT, material, pos1);
        Bad_Hole bad_hole2 = new Bad_Hole(Main.HOLE_RADIUS, Main.HOLE_HEIGHT, material, pos2);
        Bad_Hole bad_hole3 = new Bad_Hole(Main.HOLE_RADIUS, Main.HOLE_HEIGHT, material, pos3);

        bad_holes = new Bad_Hole[]{
                bad_hole1,
                bad_hole2,
                bad_hole3
        };

    }

    private void addElasticObstacles() {

        Image obstaclePattern = new Image("elastic_texture.jpg");
        PhongMaterial mat = new PhongMaterial();
        mat.setDiffuseMap(obstaclePattern);
        Translate pos1 = new Translate(Main.PODIUM_WIDTH / 5 - 50, -55, Main.PODIUM_WIDTH / 5 - 50);
        Translate pos2 = new Translate(-(Main.PODIUM_WIDTH / 5 - 50), -55, -(Main.PODIUM_WIDTH / 5 - 50));

        Elastic_Obstacle obstacle1 = new Elastic_Obstacle(50, 100, mat, pos1);
        Elastic_Obstacle obstacle2 = new Elastic_Obstacle(50, 100, mat, pos2);

        elastic_obstacles = new Elastic_Obstacle[]{
                obstacle1,
                obstacle2
        };

    }

    private void addTime() {
        root_time = new Group();
        SubScene subScene = new SubScene(root_time, WINDOW_WIDTH, WINDOW_HEIGHT, true, SceneAntialiasing.BALANCED);
        subScene.setDepthTest(DepthTest.DISABLE);

        timeText = new Text("" + (int) timePassed);
        timeText.setFill(Color.MEDIUMVIOLETRED);
        timeText.setStyle("-fx-font:40 ariel;");
        timeText.getTransforms().addAll(
                new Translate(WINDOW_WIDTH / 2, WINDOW_HEIGHT * 9 / 10)
        );

        this.root_time.getChildren().addAll(timeText);
        this.main_root.getChildren().addAll(subScene);
    }

    @Override
    public void start(Stage stage) throws IOException {
        this.root = new Group();

        this.curTerrain = "klasican";
        startTimer = false;

        this.was_topview = false;
        //IZBOR LOPTICA

        Text textLoptica = new Text("IZBABERI LOPTICU");
        textLoptica.setFont(Font.font("verdana", 30));
        textLoptica.getTransforms().addAll(new Translate(WINDOW_WIDTH / 2 - WINDOW_WIDTH / 6, WINDOW_HEIGHT / 7));
        textLoptica.setFill(Color.ORANGE);


        Button spora = new Button("Spora loptica");
        spora.getTransforms().addAll(new Translate(WINDOW_WIDTH * 4 / 9, WINDOW_HEIGHT / 4));

        Button srednja = new Button("Loptica srednje brzine");
        srednja.getTransforms().addAll(new Translate(WINDOW_WIDTH * 4 / 9 - WINDOW_WIDTH / 40, WINDOW_HEIGHT / 2));

        Button brza = new Button("Brza loptica");
        brza.getTransforms().addAll(new Translate(WINDOW_WIDTH * 4 / 9 + WINDOW_WIDTH / 80, WINDOW_HEIGHT * 3 / 4));

        Group izborLoptica = new Group();
        izborLoptica.getChildren().addAll(spora, srednja, brza, textLoptica);

        Image menuPattern = new Image(Main.class.getClassLoader().getResourceAsStream("menu_wallpaper.jpg"));
        ImagePattern menuBackground = new ImagePattern(menuPattern);

        Scene ballMenu = new Scene(izborLoptica, Main.WINDOW_WIDTH, Main.WINDOW_HEIGHT, menuBackground);

        stage.setScene(ballMenu);

        ///////////////

        //IZBOR TERENA

        Text textTeren = new Text("IZBABERI TEREN");
        textTeren.setFont(Font.font("verdana", 30));
        textTeren.getTransforms().addAll(new Translate(WINDOW_WIDTH / 2 - WINDOW_WIDTH / 6, WINDOW_HEIGHT / 7));
        textTeren.setFill(Color.ORANGE);


        Button klasican = new Button("Klasican teren");
        klasican.getTransforms().addAll(new Translate(WINDOW_WIDTH * 4 / 9, WINDOW_HEIGHT / 4));

        Button trava = new Button("Travnati teren");
        trava.getTransforms().addAll(new Translate(WINDOW_WIDTH * 4 / 9, WINDOW_HEIGHT / 2));

        Button pesak = new Button("Pescani teren");
        pesak.getTransforms().addAll(new Translate(WINDOW_WIDTH * 4 / 9, WINDOW_HEIGHT * 3 / 4));

        Group izborTerena = new Group();
        izborTerena.getChildren().addAll(klasican, trava, pesak, textTeren);

        Scene terrainMenu = new Scene(izborTerena, Main.WINDOW_WIDTH, Main.WINDOW_HEIGHT, menuBackground);

        ///////////////

        this.numberOfTries = 5;
        this.myTokens = new ArrayList<>();

        this.game_scene = new SubScene(this.root, Main.WINDOW_WIDTH, Main.WINDOW_HEIGHT, true, SceneAntialiasing.BALANCED);

        addFences();
        Translate pos1_token = new Translate(0, -500, -80);
        Translate pos2_token = new Translate(0, 500, -80);
        Translate pos3_token = new Translate(-500, 0, -80);
        Translate pos4_token = new Translate(500, 0, -80);
        addTokens(pos1_token, pos2_token, pos3_token, pos4_token);
        addLamp();

        Translate pos1 = new Translate(500, -105, 500);
        Translate pos2 = new Translate(-500, -105, 500);
        Translate pos3 = new Translate(500, -105, -500);
        Translate pos4 = new Translate(-500, -105, -500);
        addObstacles(pos1, pos2, pos3, pos4);


        Box podium = new Box(Main.PODIUM_WIDTH, Main.PODIUM_HEIGHT, Main.PODIUM_DEPTH);
        podium.setMaterial(new PhongMaterial(Color.BLUE));

        camera = new PerspectiveCamera(true);
        camera.setFarClip(Main.CAMERA_FAR_CLIP);

        this.zoom = new Translate(0, 0, CAMERA_Z);
        this.sphereX = new Rotate(Main.CAMERA_X_ANGLE, Rotate.X_AXIS);
        this.sphereY = new Rotate(0, Rotate.Y_AXIS);
        this.pan = new Translate(0, 0, 0);
        this.tilt = new Rotate(0, Rotate.X_AXIS);

        camera.getTransforms().addAll(this.pan, this.sphereY, this.sphereX, this.zoom, this.tilt);

        this.root.getChildren().add(camera);
        game_scene.setCamera(camera);


        ballMaterial = new PhongMaterial(Color.RED);
        ballPosition = new Translate(-(Main.PODIUM_WIDTH / 2 - 2 * Main.BALL_RADIUS), -(Main.BALL_RADIUS + Main.PODIUM_HEIGHT / 2), Main.PODIUM_DEPTH / 2 - 2 * Main.BALL_RADIUS);
        this.ball = new Ball(Main.BALL_RADIUS, ballMaterial, ballPosition);


        top_view = new PerspectiveCamera(true);
        top_view.setFarClip(CAMERA_FAR_CLIP);
        top_view.getTransforms().addAll(new Translate(0, -2000, 0), ballPosition, new Rotate(-90, Rotate.X_AXIS));

        double x = (Main.PODIUM_WIDTH / 2 - 2 * Main.HOLE_RADIUS);
        double z = -(Main.PODIUM_DEPTH / 2 - 2 * Main.HOLE_RADIUS);

        Translate holePosition = new Translate(x, -30, z);
        Material holeMaterial = new PhongMaterial(Color.YELLOW);

        this.hole = new Hole(Main.HOLE_RADIUS, Main.HOLE_HEIGHT, holeMaterial, holePosition);


        Translate pos1_badhole = new Translate(0, -30, 0);
        Translate pos2_badhole = new Translate(800, -30, 0);
        Translate pos3_badhole = new Translate(-800, -30, 0);
        addBadHoles(pos1_badhole, pos2_badhole, pos3_badhole);

        addElasticObstacles();


        this.arena = new Arena();
        this.arena.getChildren().add(podium);
        this.arena.getChildren().add(this.ball);
        this.arena.getChildren().addAll(this.hole);
        this.arena.getChildren().addAll(this.fences);
        this.arena.getChildren().addAll(this.myTokens);
        this.arena.getChildren().addAll(this.sky_lamp);

        this.arena.getChildren().addAll(this.bad_holes);
        //this.arena.getChildren().addAll(this.obstacles);

        this.arena.getChildren().addAll(this.elastic_obstacles);

        this.root.getChildren().add(this.arena);

        Timer timer = new Timer(deltaSeconds -> {

            if (startTimer && timePassed >= 0) {
                timePassed -= deltaSeconds;
                root_time.getChildren().remove(timeText);
                timeText.setText("" + (int) timePassed);
                root_time.getChildren().addAll(this.timeText);
                if (timePassed <= 0) {
                    this.arena.getChildren().remove(this.ball);
                    Main.this.ball = null;
                    addEndGameScreen();
                }

            }
            this.arena.update(0.995);
            //double acc = Math.sqrt(Math.pow(this.arena.getXAngle(), 2) + Math.pow(this.arena.getZAngle(), 2));
            speedLine.setEndY(this.arena.getXAngle() / MAX_ANGLE_OFFSET * 75);
            speedLine.setEndX(this.arena.getZAngle() / MAX_ANGLE_OFFSET * 75);
            if (Main.this.ball != null) {

                boolean outOfArena = Main.this.ball.update(deltaSeconds, Main.PODIUM_DEPTH / 2, -Main.PODIUM_DEPTH / 2, -Main.PODIUM_WIDTH / 2, Main.PODIUM_WIDTH / 2, this.arena.getXAngle(), this.arena.getZAngle(), Main.MAX_ANGLE_OFFSET, Main.REAL_MAX_ACCELERATION, Main.DAMP);

                Arrays.stream(this.fences).anyMatch(fence -> fence.handleCollision(this.ball));
                boolean isInHole = this.hole.handleCollision(this.ball);

                if (obstacles != null)
                    Arrays.stream(this.obstacles).anyMatch(obstacle -> obstacle.handleCollision(this.ball));

                Arrays.stream(this.elastic_obstacles).anyMatch(elastic_obstacle -> elastic_obstacle.handleCollision(this.ball));

                for (int i = 0; i < myTokens.size(); ++i) {
                    if (myTokens.get(i).handleCollision(ball)) {
                        this.points += 1;
                        this.root_utils.getChildren().remove(this.pointsText);
                        this.root_utils.getChildren().addAll(addPoints(this.points));
                        this.arena.getChildren().remove(myTokens.get(i));
                        myTokens.remove(i);
                    }
                }

                boolean isInBadHole = Arrays.stream(this.bad_holes).anyMatch(bad_hole -> bad_hole.handleCollision(this.ball));

                if (outOfArena || isInHole || isInBadHole) {
                    if (isInHole) {
                        this.points += 5;
                        this.root_utils.getChildren().remove(this.pointsText);
                        this.root_utils.getChildren().addAll(addPoints(this.points));
                    } else if (isInBadHole) {
                        if (this.points - 3 >= 0)
                            this.points -= 3;
                        else
                            this.points = 0;
                        this.root_utils.getChildren().remove(this.pointsText);
                        this.root_utils.getChildren().addAll(addPoints(this.points));
                    }
                    this.arena.getChildren().remove(this.ball);
                    Main.this.ball = null;
                    this.root_utils.getChildren().remove(this.tries[numberOfTries - 1]);
                    this.numberOfTries--;
                    if (this.numberOfTries <= 0) {
                        //stage.close();
                        startTimer = false;
                        addEndGameScreen();
                    } else {
                        restart_game();
                    }
                }
            }
        });
        timer.start();


        main_root = new Group(game_scene);
        main_scene = new Scene(main_root, Main.WINDOW_WIDTH, Main.WINDOW_HEIGHT, true, SceneAntialiasing.BALANCED);

        Image backgroundPattern = new Image(Main.class.getClassLoader().getResourceAsStream("background.jpg"));
        ImagePattern background = new ImagePattern(backgroundPattern);
        main_scene.setFill(background);

        main_scene.addEventHandler(KeyEvent.ANY, this);
        main_scene.addEventHandler(MouseEvent.ANY, this);
        main_scene.addEventHandler(ScrollEvent.ANY, this);
        main_scene.addEventHandler(KeyEvent.ANY, event -> this.arena.handleKeyEvent(event, Main.MAX_ANGLE_OFFSET));

        addTries();
        addMap();
        addTime();

        stage.setTitle("Rolling Ball");

        //IZBOR LOPTICA

        spora.setOnAction(actionEvent -> {
            stage.setScene(terrainMenu);
        });

        srednja.setOnAction(actionEvent -> {
            REAL_MAX_ACCELERATION = 450;
            Image ball_pattern = new Image("med_ball.jpg");
            ballMaterial = new PhongMaterial();
            ballMaterial.setDiffuseMap(ball_pattern);
            this.ball.setMaterial(ballMaterial);
            stage.setScene(terrainMenu);
        });

        brza.setOnAction(actionEvent -> {
            REAL_MAX_ACCELERATION = 500;
            Image ball_pattern = new Image("fast_ball.jpg");
            ballMaterial = new PhongMaterial();
            ballMaterial.setDiffuseMap(ball_pattern);
            this.ball.setMaterial(ballMaterial);
            stage.setScene(terrainMenu);
        });

        ///////////////

        //IZBOR TERENA

        klasican.setOnAction(actionEvent -> {
            addObstacles(pos1, pos2, pos3, pos4);
            arena.getChildren().addAll(this.obstacles);
            startTimer = true;
            stage.setScene(main_scene);
        });

        trava.setOnAction(actionEvent -> {
            this.curTerrain = "trava";

            Image pattern = new Image(Main.class.getClassLoader().getResourceAsStream("sky_background.jpg"));
            ImagePattern background_new = new ImagePattern(pattern);

            main_scene.setFill(background_new);

            PhongMaterial podium_mat = new PhongMaterial();
            podium_mat.setDiffuseMap(new Image("grass_podium.jpg"));
            podium.setMaterial(podium_mat);
            hole.getTransforms().addAll(new Translate(-PODIUM_WIDTH / 2 + 2 * HOLE_RADIUS, 0, 0));

            this.arena.getChildren().remove(this.ball);
            Main.this.ball = null;
            restart_game();

            Translate pos1_new = new Translate(0, -105, -500);
            Translate pos2_new = new Translate(0, -105, 500);
            Translate pos3_new = new Translate(-500, -105, 0);
            Translate pos4_new = new Translate(500, -105, 0);
            addObstacles(pos1_new, pos2_new, pos3_new, pos4_new);
            arena.getChildren().addAll(this.obstacles);
            PhongMaterial obstacleMat = new PhongMaterial();
            obstacleMat.setDiffuseMap(new Image("tree_texture.jpg"));
            Arrays.stream(this.obstacles).allMatch(obstacle -> obstacle.changeMat(obstacleMat));

            startTimer = true;
            stage.setScene(main_scene);
        });

        pesak.setOnAction(actionEvent -> {
            this.curTerrain = "pesak";

            Image pattern = new Image(Main.class.getClassLoader().getResourceAsStream("nightsky_background.jpg"));
            ImagePattern background_new = new ImagePattern(pattern);

            main_scene.setFill(background_new);

            PhongMaterial podium_mat = new PhongMaterial();
            podium_mat.setDiffuseMap(new Image("sand_podium.jpg"));
            podium.setMaterial(podium_mat);
            hole.getTransforms().addAll(new Translate(-PODIUM_WIDTH + 4 * HOLE_RADIUS, 0, 0));
            this.arena.getChildren().remove(this.ball);
            Main.this.ball = null;

            Translate pos1_new = new Translate(650, -105, 650);
            Translate pos2_new = new Translate(-650, -105, 650);
            Translate pos3_new = new Translate(650, -105, -650);
            Translate pos4_new = new Translate(-650, -105, -650);
            addObstacles(pos1_new, pos2_new, pos3_new, pos4_new);
            arena.getChildren().addAll(this.obstacles);
            PhongMaterial obstacleMat = new PhongMaterial();
            obstacleMat.setDiffuseMap(new Image("sandstone_texture.jpg"));
            Arrays.stream(this.obstacles).allMatch(obstacle -> obstacle.changeMat(obstacleMat));

            restart_game();
            startTimer = true;
            stage.setScene(main_scene);
        });

        //////////////

        //stage.setScene(main_scene);
        stage.show();
    }

    public void restart_game() {

        this.arena.update(0);

        //Material ballMaterial = new PhongMaterial(Color.RED);

        Translate ballPosition = new Translate(-(Main.PODIUM_WIDTH / 2 - 2 * Main.BALL_RADIUS), -(Main.BALL_RADIUS + Main.PODIUM_HEIGHT / 2), Main.PODIUM_DEPTH / 2 - 2 * Main.BALL_RADIUS);

        if (this.curTerrain.equals("trava")) {
            ballPosition = new Translate(0, -(Main.BALL_RADIUS + Main.PODIUM_HEIGHT / 2), (Main.PODIUM_WIDTH / 2 - 2 * Main.BALL_RADIUS));

           // if (was_topview) {
                top_view = new PerspectiveCamera(true);
                top_view.setFarClip(CAMERA_FAR_CLIP);
                top_view.getTransforms().addAll(new Translate(0, -2000, 0), ballPosition, new Rotate(-90, Rotate.X_AXIS));
                //this.game_scene.setCamera(top_view);
            //}

        } else if (this.curTerrain.equals("pesak")) {
            ballPosition = new Translate((Main.PODIUM_WIDTH / 2 - 2 * Main.BALL_RADIUS), -(Main.BALL_RADIUS + Main.PODIUM_HEIGHT / 2), Main.PODIUM_DEPTH / 2 - 2 * Main.BALL_RADIUS);

            //if (was_topview) {
                top_view = new PerspectiveCamera(true);
                top_view.setFarClip(CAMERA_FAR_CLIP);
                top_view.getTransforms().addAll(new Translate(0, -2000, 0), ballPosition, new Rotate(-90, Rotate.X_AXIS));
                //this.game_scene.setCamera(top_view);
            //}
        }

        this.ball = new Ball(Main.BALL_RADIUS, ballMaterial, ballPosition);

        if (was_topview) {
            top_view = new PerspectiveCamera(true);
            top_view.setFarClip(CAMERA_FAR_CLIP);
            top_view.getTransforms().addAll(new Translate(0, -2000, 0), ballPosition, new Rotate(-90, Rotate.X_AXIS));
            this.game_scene.setCamera(top_view);
        }

        this.arena.getChildren().add(this.ball);
        for (int i = 0; i < myTokens.size(); ++i) {
            this.arena.getChildren().remove(myTokens.get(i));
        }
        this.arena.getChildren().remove(myTokens);
        this.myTokens = new ArrayList<>();

        Translate pos1 = new Translate(0, -500, -80);
        Translate pos2 = new Translate(0, 500, -80);
        Translate pos3 = new Translate(-500, 0, -80);
        Translate pos4 = new Translate(500, 0, -80);


        if (this.curTerrain.equals("trava")) {

            pos1 = new Translate(500, 500, -80);
            pos2 = new Translate(-500, 500, -80);
            pos3 = new Translate(500, -500, -80);
            pos4 = new Translate(-500, -500, -80);

        } else if (this.curTerrain.equals("pesak")) {
            pos1 = new Translate(450, 450, -80);
            pos2 = new Translate(-450, 450, -80);
            pos3 = new Translate(450, -450, -80);
            pos4 = new Translate(-450, -450, -80);
        }

        addTokens(pos1, pos2, pos3, pos4);
        this.arena.getChildren().addAll(myTokens);
    }

    public static void main(String[] args) {
        launch();
    }


}