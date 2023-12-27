package mai.project.foreground_service

import android.graphics.Color
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder

class CountdownAdapter : ListAdapter<Pair<Int, Long>, ViewHolder>(diffUtil) {

    private class MyViewHolder(
        val textView: TextView
    ) : ViewHolder(textView) {
        companion object {
            fun create(parent: ViewGroup): MyViewHolder {
                val textView = TextView(parent.context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    setTextColor(Color.BLACK)
                    textSize = 16f
                }
                return MyViewHolder(textView)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return MyViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        (holder as MyViewHolder).textView.apply {
            text = "倒數計時 ${item.first}：${item.second}"
        }
    }

    companion object {
        private val diffUtil = object : DiffUtil.ItemCallback<Pair<Int, Long>>() {
            override fun areItemsTheSame(
                oldItem: Pair<Int, Long>,
                newItem: Pair<Int, Long>
            ): Boolean {
                return oldItem.first == newItem.first
            }

            override fun areContentsTheSame(
                oldItem: Pair<Int, Long>,
                newItem: Pair<Int, Long>
            ): Boolean {
                return oldItem.second == newItem.second
            }
        }
    }
}