package de.leyn.noteapp.presentation.add_edit_note

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.doOnTextChanged
import de.leyn.noteapp.App
import de.leyn.noteapp.R
import de.leyn.noteapp.databinding.ActivitySingleNoteBinding
import de.leyn.noteapp.domain.model.Note
import de.leyn.noteapp.domain.model.NoteColors
import de.leyn.noteapp.data.repositories.RoomNoteRepositoryImpl
import de.leyn.noteapp.extensions.convertToDateTimeString
import de.leyn.noteapp.toEditable
import de.leyn.noteapp.presentation.viewmodel.NoteViewModel
import de.leyn.noteapp.presentation.viewmodel.ViewModelFactory
import java.util.*

class SingleNoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySingleNoteBinding
    private lateinit var viewModel: NoteViewModel

    private var currentHexBackgroundColor: String = NoteColors.YELLOW.hexColor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySingleNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val repository = RoomNoteRepositoryImpl(applicationContext)
        viewModel = ViewModelFactory(repository).create(NoteViewModel::class.java)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            val backArrow = ResourcesCompat.getDrawable(resources, R.drawable.arrow_back, null)
            backArrow?.colorFilter = PorterDuffColorFilter(
                resources.getColor(R.color.onPrimary, null),
                PorterDuff.Mode.SRC_ATOP
            )
            setHomeAsUpIndicator(backArrow)
        }
        binding.toolbar.overflowIcon?.apply {
            colorFilter = PorterDuffColorFilter(
                resources.getColor(R.color.onPrimary, null),
                PorterDuff.Mode.SRC_ATOP
            )
        }

        if (!isNewNote()) {
            val note = intent.getSerializableExtra(App.INTENT_NOTE) as Note
            viewModel.singleNote = note
            viewModel.isNewNote = false
            currentHexBackgroundColor = note.color

            applyContentAndColorToView(note)
        } else {
            viewModel.isNewNote = true
            supportActionBar?.title = resources.getString(R.string.new_note)
        }

        binding.titleEditText.doOnTextChanged { text, _, _, _ ->
            supportActionBar?.title = text
        }
    }

    private fun isNewNote() = !intent.hasExtra(App.INTENT_NOTE)

    private fun applyContentAndColorToView(note: Note) {
        binding.titleEditText.text = note.title.toEditable()
        binding.textEditText.text = note.text.toEditable()
        supportActionBar?.title = note.title
        setLayoutBackgroundColorTo(note.color)
    }

    override fun onSupportNavigateUp(): Boolean {
        if (viewModel.isNewNote) {
            if (!isEmptyContent()) {
                viewModel.insertNote(
                    Note(
                        title = binding.titleEditText.text.toString(),
                        text = binding.textEditText.text.toString(),
                        createdDate = Date().convertToDateTimeString(),
                        lastEditedDate = Date().convertToDateTimeString(),
                        color = currentHexBackgroundColor
                    )
                )
            }
        } else {
            if (!isIdenticalToOriginalNote()) {
                viewModel.singleNote.apply {
                    title = binding.titleEditText.text.toString().trim()
                    text = binding.textEditText.text.toString().trim()
                    lastEditedDate = Date().convertToDateTimeString()
                    color = currentHexBackgroundColor
                }
            }
            viewModel.updateNote()
        }

        onBackPressed()
        return true
    }

    private fun isEmptyContent(): Boolean {
        return binding.textEditText.text.toString()
            .isEmpty() && binding.titleEditText.text.toString().isEmpty()
    }

    private fun isIdenticalToOriginalNote(): Boolean {
        val currentTitle = binding.titleEditText.text.toString()
        val currentText = binding.textEditText.text.toString()
        return currentTitle == viewModel.singleNote.title &&
                currentText == viewModel.singleNote.text &&
                currentHexBackgroundColor == viewModel.singleNote.color
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.color_category_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.colorBlue -> {
                setLayoutBackgroundColorTo(NoteColors.BLUE.hexColor)
                currentHexBackgroundColor = NoteColors.BLUE.hexColor
                true
            }
            R.id.colorGreen -> {
                setLayoutBackgroundColorTo(NoteColors.GREEN.hexColor)
                currentHexBackgroundColor = NoteColors.GREEN.hexColor
                true
            }
            R.id.colorRed -> {
                setLayoutBackgroundColorTo(NoteColors.RED.hexColor)
                currentHexBackgroundColor = NoteColors.RED.hexColor
                true
            }
            R.id.colorYellow -> {
                setLayoutBackgroundColorTo(NoteColors.YELLOW.hexColor)
                currentHexBackgroundColor = NoteColors.YELLOW.hexColor
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setLayoutBackgroundColorTo(hexColor: String) {
        binding.layout.setBackgroundColor(Color.parseColor(hexColor))
    }
}