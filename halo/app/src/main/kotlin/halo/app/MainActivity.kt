package halo.app

import android.app.Activity
import android.os.Bundle
import android.widget.ScrollView
import android.widget.TextView
import halo.prompts.HaloPrompts

/**
 * Placeholder screen for the skeleton: shows Halo's persona loaded from the core module,
 * which proves the app and core are wired together. Conversation arrives in step 1.
 */
class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val padding = (24 * resources.displayMetrics.density).toInt()
        val text = TextView(this).apply {
            setPadding(padding, padding, padding, padding)
            textSize = 16f
            text = "Halo 뼈대가 준비됐어요.\n대화 기능은 1단계에서 붙여요.\n\n" + HaloPrompts.persona
        }
        setContentView(ScrollView(this).apply { addView(text) })
    }
}
