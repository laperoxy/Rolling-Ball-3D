package com.example.projekat_2.arena;

import javafx.geometry.Point3D;
import javafx.scene.paint.Material;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Translate;

public class Ball extends Sphere {
    private Translate position;
    private Point3D speed;

    public Ball(double radius, Material material, Translate position) {
        super(radius);
        super.setMaterial(material);

        this.position = position;

        super.getTransforms().add(this.position);

        this.speed = new Point3D(0, 0, 0);
    }

    public Point3D getSpeed() {
        return speed;
    }

    public void setSpeed(Point3D speed) {
        this.speed = speed;
    }

    public boolean update(
            double deltaSeconds,
            double top,
            double bottom,
            double left,
            double right,
            double xAngle,
            double zAngle,
            double maxAngleOffset,
            double maxAcceleration,
            double damp
    ) {
        double newPositionX = this.position.getX() + this.speed.getX() * deltaSeconds;
        double newPositionZ = this.position.getZ() + this.speed.getZ() * deltaSeconds;

        this.position.setX(newPositionX);
        this.position.setZ(newPositionZ);

        double accelerationX = maxAcceleration * zAngle / maxAngleOffset;
        double accelerationZ = -maxAcceleration * xAngle / maxAngleOffset;

        double newSpeedX = (this.speed.getX() + accelerationX * deltaSeconds) * damp;
        double newSpeedZ = (this.speed.getZ() + accelerationZ * deltaSeconds) * damp;

        this.speed = new Point3D(newSpeedX, 0, newSpeedZ);

        boolean xOutOfBounds = (newPositionX > right) || (newPositionX < left);
        boolean zOutOfBounds = (newPositionZ > top) || (newPositionZ < bottom);

        return xOutOfBounds || zOutOfBounds;
    }

    public double getXPos() {
        return this.position.getX();
    }

    public double getZPos() {
        return this.position.getZ();
    }

}
