package com.pear0.filter

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.TypedArray
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import com.yalantis.filter.R
import kotlinx.android.synthetic.main.filter2.view.*
import java.io.File


/**
 * Created by William on 7/22/2017.
 */
class Filter<E : FilterItem> : FrameLayout {

    open class Adapter<E : FilterItem>(val context: Context, val items: Array<E>)

    internal class ItemViewHolder(val index: Int, val item: FilterItem) {
        var isSelected: Boolean = false
    }

    internal class ExpandedItemViewHolder(val root: FilterItemView) : RecyclerView.ViewHolder(root)

    internal class CollapsedItemViewHolder(val root: FilterItemView) : RecyclerView.ViewHolder(root)

    internal inner class ExpandedItemAdapter : RecyclerView.Adapter<ExpandedItemViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpandedItemViewHolder {
            return ExpandedItemViewHolder(FilterItemView(parent.context))
        }

        override fun onBindViewHolder(holder: ExpandedItemViewHolder, position: Int) {
            val item = adapter?.let { it.items[position] } ?: return

            holder.root.setText(item.text)
            holder.root.index = position
            holder.root.color = item.color

            holder.root.setOnClickListener {
                onItemClicked(position)
            }
        }

        override fun getItemCount(): Int {
            return adapter?.items?.size ?: 0
        }
    }

    internal inner class CollapsedItemAdapter : RecyclerView.Adapter<CollapsedItemViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CollapsedItemViewHolder {
            return CollapsedItemViewHolder(FilterItemView(parent.context))
        }

        override fun onBindViewHolder(holder: CollapsedItemViewHolder, position: Int) {
            val item = items.filter { it.isSelected }[position]


            holder.root.setCollapsed()

            holder.root.index = item.index
            holder.root.color = item.item.color

            holder.root.setOnClickListener {
                onItemClicked(item.index, false)
            }
        }

        override fun getItemCount(): Int {
            return items.count { it.isSelected }
        }
    }

    private var adapter: Adapter<E>? = null
    private val items = ArrayList<ItemViewHolder>()
    private var isExpanded = false

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleRes: Int) : super(context, attrs, defStyleRes) {
        LayoutInflater.from(context).inflate(R.layout.filter2, this, true)


        with(container_expanded) {
            layoutManager = LinearLayoutManager(context)
            adapter = ExpandedItemAdapter()
        }

        with(container_collapsed) {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = CollapsedItemAdapter()

        }

        collapse_button.setOnClickListener {
            Log.i("Test", "Button Click")
            if (isExpanded) collapse()
            else expand()
        }
    }

    private fun performAnimations(reversed: Boolean) {

        Log.i("Filter", "Views: ${(0 until childCount).map { getChildAt(it) }.mapNotNull { it as? FilterItemView }.map { it.index }}")

        val selectedItems = items.filter { it.isSelected }
        for (selectedIndex in selectedItems.indices) {
            val item = selectedItems[selectedIndex]

            val expandedView = container_expanded.findViewHolderForAdapterPosition(item.index) ?: continue
            val collapsedView = container_collapsed.findViewHolderForAdapterPosition(selectedIndex) ?: continue

            val view = FilterItemView(context)
            this.addView(view)

            view.setTransitioning(collapsedView.itemView as FilterItemView, expandedView.itemView as FilterItemView, 0, reversed)
        }

    }

    fun expand() {
        if (isExpanded) return
        isExpanded = true

        Log.i("Filter", "Expand")

        val height = container_expanded.measuredHeight
        with(ValueAnimator.ofInt(height, resources.getDimensionPixelSize(R.dimen.container_expanded_height))) {
            addUpdateListener {
                val fHeight = it.animatedValue as Int
                Log.i("Height", "$fHeight")

                val layoutParams = container_expanded.layoutParams
                layoutParams.height = fHeight
                container_expanded.layoutParams = layoutParams
                container_expanded.invalidate()
            }
            start()
        }

        performAnimations(false)

    }

    fun collapse() {
        if (!isExpanded) return
        isExpanded = false

        Log.i("Filter", "Collapse")

        val height = container_expanded.measuredHeight
        with(ValueAnimator.ofFloat(0f, 1f)) {
            addUpdateListener {
                val fHeight = (height * (1 - it.animatedFraction)).toInt()
                Log.i("Height", "$fHeight from ${it.animatedFraction}")

                val layoutParams = container_expanded.layoutParams
                layoutParams.height = fHeight
                container_expanded.layoutParams = layoutParams
                container_expanded.invalidate()

            }
            start()
        }

        performAnimations(true)

    }

    fun onItemClicked(position: Int, newState: Boolean? = null) {
        Log.i("Clicked", "$position")
        val item = items[position]

        item.isSelected = newState ?: !item.isSelected

        container_collapsed.adapter.notifyDataSetChanged()


    }

    fun setAdapter(adapter: Adapter<E>) {
        this.adapter = adapter
        items.clear()
        items.ensureCapacity(adapter.items.size)
        adapter.items.mapIndexedTo(items) { i, it -> ItemViewHolder(i, it) }


        container_expanded.adapter.notifyDataSetChanged()

    }



}