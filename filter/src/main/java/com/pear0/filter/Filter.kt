package com.pear0.filter

import android.animation.ValueAnimator
import android.content.Context
import android.os.Handler
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.yalantis.filter.R
import kotlinx.android.synthetic.main.filter2.view.*
import android.os.Looper



/**
 * Created by William on 7/22/2017.
 */
class Filter<E: FilterItem> : FrameLayout {

    open class Adapter<E: FilterItem>(val context: Context, val items: Array<E>)

    internal class ItemViewHolder(val item: FilterItem) {
        var isSelected: Boolean = false
        var collapsedView: FilterItemView? = null
    }

    internal class ExpandedItemViewHolder(val root: FilterItemView) : RecyclerView.ViewHolder(root)

    internal inner class ExpandedItemAdapter : RecyclerView.Adapter<ExpandedItemViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpandedItemViewHolder {
            return ExpandedItemViewHolder(FilterItemView(parent.context))
        }

        override fun onBindViewHolder(holder: ExpandedItemViewHolder, position: Int) {
            holder.root.setText(adapter?.let { it.items[position].text } ?: "")
        }

        override fun getItemCount(): Int {
            return adapter?.items?.size ?: 0
        }
    }

    private var adapter: Adapter<E>? = null
    private val items = ArrayList<ItemViewHolder>()

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleRes: Int) : super(context, attrs, defStyleRes) {
        LayoutInflater.from(context).inflate(R.layout.filter2, this, true)

        with(container_expanded) {
            layoutManager = LinearLayoutManager(context)
            adapter = ExpandedItemAdapter()

        }
    }

    fun expand() {


    }

    fun collapse() {
        container_collapsed.removeAllViews()

        for (item in items.filter { it.isSelected }) {
            if (item.collapsedView == null) {
                item.collapsedView = FilterItemView(context)
            }

            item.collapsedView!!.visibility = View.INVISIBLE

            container_collapsed.addView(item.collapsedView)
        }

        Handler(Looper.getMainLooper()).post {


        }

    }

    fun onItemClicked(position: Int, newState: Boolean? = null) {
        val item = items[position]

        item.isSelected = newState ?: !item.isSelected
    }

    fun setAdapter(adapter: Adapter<E>) {
        this.adapter = adapter
        items.clear()
        items.ensureCapacity(adapter.items.size)
        adapter.items.mapTo(items) { ItemViewHolder(it) }


        container_expanded.adapter.notifyDataSetChanged()

    }



}