package de.devmil.paperlaunch.config;

public enum LauncherGravity {
    Top(0),
    Center(1),
    Bottom(2);

    private int mValue;

    LauncherGravity(int value) {
        mValue = value;
    }

    public int getValue() {
        return mValue;
    }

    public static LauncherGravity fromValue(int value) {
        switch(value) {
            case 0:
                return Top;
            case 1:
                return Center;
            case 2:
                return Bottom;
            default:
                return Top;
        }
    }
}
