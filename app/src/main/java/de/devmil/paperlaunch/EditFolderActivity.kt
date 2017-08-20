package de.devmil.paperlaunch

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toolbar
import de.devmil.paperlaunch.view.fragments.EditFolderFragment

class EditFolderActivity : Activity() {

    private var toolbar: Toolbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_folder)

        toolbar = findViewById(R.id.activity_edit_folder_toolbar) as Toolbar

        setActionBar(toolbar)
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        actionBar!!.setDisplayShowHomeEnabled(true)

        var folderId: Long = -1
        if (intent.hasExtra(ARG_FOLDERID)) {
            folderId = intent.getLongExtra(ARG_FOLDERID, -1)
        }

        val fragment: EditFolderFragment?

        if (savedInstanceState == null) {
            val trans = fragmentManager.beginTransaction()
            fragment = EditFolderFragment.newInstance(folderId)
            trans.add(R.id.activity_edit_folder_folder_fragment, fragment)
            trans.commit()
        } else {
            fragment = fragmentManager.findFragmentById(R.id.activity_edit_folder_folder_fragment) as EditFolderFragment
        }

        fragment.setListener { title = it }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
        // Respond to the action bar's Up/Home button
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {

        private val ARG_FOLDERID = "folderId"

        fun createLaunchIntent(context: Context, folderId: Long): Intent {
            val result = Intent(context, EditFolderActivity::class.java)
            result.putExtra(ARG_FOLDERID, folderId)
            return result
        }
    }
}
