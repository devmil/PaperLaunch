package de.devmil.paperlaunch.model

import android.content.Intent
import android.graphics.drawable.Drawable

/**
 * @brief interface for accessing data that gets provided via the context
 *        this interface can be used to push the Context dependency to the edge
 *        and keep the core (j)unit testable
 */
interface IContextAccess {

    /**
     * @brief   returns a drawable with the given id
     * @param id    the id of the drawable
     * @param themed    when set to true the current theme gets applied, otherwise the default
     *                  "getDrawable" is called
     */
    fun getDrawable(id: Int, themed : Boolean) : Drawable

    /**
     * @brief   returns the application name the given Intent is pointing to
     */
    fun getAppName(intent: Intent): String?

    /**
     * @brief   returns the application icon the given Intent is pointing to
     */
    fun getAppIcon(intent: Intent): Drawable
}