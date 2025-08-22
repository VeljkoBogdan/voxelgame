package io.github.illuminatijoe.voxelgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

public class Player {
    public Vector3 position;
    public PerspectiveCamera camera;

    private float yaw = 0f;
    private float pitch = 0f;

    private float speed = 10f;         // movement speed
    private float mouseSensitivity = 0.2f;

    public Player() {
        position = new Vector3(0, 64, 0); // start somewhere above ground

        camera = new PerspectiveCamera(70, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(position);
        camera.near = 0.1f;
        camera.far = 1000f;

        Gdx.input.setCursorCatched(true); // lock & hide mouse
    }

    public void update(float delta) {
        handleMouseLook();
        handleMovement(delta);
        camera.update();
    }

    private void handleMouseLook() {
        yaw += Gdx.input.getDeltaX() * mouseSensitivity;
        pitch -= Gdx.input.getDeltaY() * mouseSensitivity; // invert Y for natural look

        pitch = MathUtils.clamp(pitch, -89, 89);

        float yawRad = yaw * MathUtils.degreesToRadians;
        float pitchRad = pitch * MathUtils.degreesToRadians;

        Vector3 direction = new Vector3();
        direction.x = MathUtils.cos(pitchRad) * MathUtils.sin(yawRad);
        direction.y = MathUtils.sin(pitchRad);
        direction.z = -MathUtils.cos(pitchRad) * MathUtils.cos(yawRad);

        camera.direction.set(direction).nor();
    }


    private void handleMovement(float delta) {
        Vector3 forward = new Vector3(camera.direction).nor();
        Vector3 right = new Vector3(forward).crs(Vector3.Y).nor();
        Vector3 up = new Vector3(0, 1, 0);

        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            position.add(forward.scl(speed * delta));
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            position.sub(forward.scl(speed * delta));
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            position.sub(right.scl(speed * delta));
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            position.add(right.scl(speed * delta));
        }
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            position.add(up.scl(speed * delta));
        }
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            position.sub(up.scl(speed * delta));
        }

        // update camera position
        camera.position.set(position);
    }
}
