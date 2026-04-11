package com.cs173.myapplication.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cs173.myapplication.R
import com.cs173.myapplication.model.Goal

class GoalAdapter(
    private var goals: List<Goal>,
    private val onAddFunds: (Goal) -> Unit
) : RecyclerView.Adapter<GoalAdapter.GoalViewHolder>() {

    class GoalViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivGoal: ImageView = view.findViewById(R.id.iv_goal_image)
        val tvName: TextView = view.findViewById(R.id.tv_goal_name)
        val tvDeadline: TextView = view.findViewById(R.id.tv_goal_deadline)
        val pbProgress: ProgressBar = view.findViewById(R.id.pb_goal_progress)
        val tvStatus: TextView = view.findViewById(R.id.tv_goal_status)
        val btnAddFunds: Button = view.findViewById(R.id.btn_add_funds)
        val cvCompletedBadge: View = view.findViewById(R.id.cv_completed_badge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_goal, parent, false)
        return GoalViewHolder(view)
    }

    override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
        val goal = goals[position]
        holder.tvName.text = goal.name
        holder.tvDeadline.text = "Deadline: ${goal.deadline}"
        holder.tvStatus.text = "Rs. ${goal.savedAmount.toInt()} / Rs. ${goal.targetAmount.toInt()}"
        
        val progress = ((goal.savedAmount / goal.targetAmount) * 100).toInt()
        holder.pbProgress.progress = progress

        if (progress >= 100) {
            holder.pbProgress.visibility = View.GONE
            holder.cvCompletedBadge.visibility = View.VISIBLE
        } else {
            holder.pbProgress.visibility = View.VISIBLE
            holder.cvCompletedBadge.visibility = View.GONE
        }

        if (goal.imageUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context).load(goal.imageUrl).into(holder.ivGoal)
        } else {
            holder.ivGoal.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        holder.btnAddFunds.setOnClickListener { onAddFunds(goal) }
    }

    override fun getItemCount() = goals.size

    fun updateList(newList: List<Goal>) {
        goals = newList
        notifyDataSetChanged()
    }
}
