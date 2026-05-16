package com.example.givepathopendirselectfiledemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.givepathopendirselectfiledemo.databinding.ActivityMainBinding
import com.example.givepathopendirselectfiledemo.domain.handler.PathOpenHandler
import com.example.givepathopendirselectfiledemo.domain.store.PathOpenState
import com.example.givepathopendirselectfiledemo.domain.store.PathOpenStore
import com.example.givepathopendirselectfiledemo.infra.system.PathOpenSystemApi
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val store = PathOpenStore()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val handler = PathOpenHandler(
            store = store,
            pathOpenSystemApi = PathOpenSystemApi(this),
        )

        binding.inputAbsolutePath.setText(store.state.value.inputPath)
        binding.inputAbsolutePath.doAfterTextChanged { editable ->
            handler.onPathInputChanged(editable?.toString().orEmpty())
        }
        binding.buttonOpenParentDir.setOnClickListener {
            handler.onOpenParentDirClick()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                store.state.collect(::render)
            }
        }
    }

    private fun render(state: PathOpenState) {
        val currentInput = binding.inputAbsolutePath.text?.toString().orEmpty()
        if (currentInput != state.inputPath) {
            binding.inputAbsolutePath.setText(state.inputPath)
            binding.inputAbsolutePath.setSelection(state.inputPath.length)
        }

        binding.textStatusValue.text = state.status
        binding.textResolvedUriValue.text = state.resolvedUri
    }
}
