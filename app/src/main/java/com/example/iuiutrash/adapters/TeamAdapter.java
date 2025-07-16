package com.example.iuiutrash.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.iuiutrash.R;
import com.example.iuiutrash.models.TeamMember;
import java.util.List;

public class TeamAdapter extends RecyclerView.Adapter<TeamAdapter.TeamViewHolder> {
    private List<TeamMember> teamMembers;

    public TeamAdapter(List<TeamMember> teamMembers) {
        this.teamMembers = teamMembers;
    }

    @NonNull
    @Override
    public TeamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_team_member, parent, false);
        return new TeamViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TeamViewHolder holder, int position) {
        TeamMember member = teamMembers.get(position);
        holder.nameTextView.setText(member.getName());
        holder.roleTextView.setText(member.getRole());
        holder.descriptionTextView.setText(member.getDescription());
        holder.imageView.setImageResource(member.getImageResource());
    }

    @Override
    public int getItemCount() {
        return teamMembers.size();
    }

    static class TeamViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView nameTextView;
        TextView roleTextView;
        TextView descriptionTextView;

        TeamViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.memberImage);
            nameTextView = itemView.findViewById(R.id.memberName);
            roleTextView = itemView.findViewById(R.id.memberRole);
            descriptionTextView = itemView.findViewById(R.id.memberDescription);
        }
    }
} 