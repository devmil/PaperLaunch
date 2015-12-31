/*
 * Copyright 2015 Devmil Solutions
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
