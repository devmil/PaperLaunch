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
package de.devmil.paperlaunch.config

interface IUserSettings {

    val sensitivityDip: Int
    val isOnRightSide: Boolean
    val isShowBackground: Boolean
    val isVibrateOnActivation: Boolean
    val launcherGravity: LauncherGravity?
    val showLogo: Boolean
    val itemScalePercent: Int
    val activationHeightPercent: Int
    val activationOffsetPosition: Int
    val isShowHint : Boolean

}
