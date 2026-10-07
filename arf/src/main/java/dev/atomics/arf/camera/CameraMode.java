package dev.atomics.arf.camera;

public enum CameraMode {
    FIRST_PERSON, THIRD_PERSON, FREE, ISOMETRIC;

    /** In isometric mode PZ's own renderer draws the frame and ARF steps aside. */
    public boolean handsControlToVanilla() { return this == ISOMETRIC; }
    /** Free/photo mode pauses input to the player. */
    public boolean pausesPlayerInput() { return this == FREE; }
}
