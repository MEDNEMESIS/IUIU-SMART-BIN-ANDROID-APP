package com.example.iuiutrash.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.iuiutrash.R;
import com.example.iuiutrash.adapters.TeamAdapter;
import com.example.iuiutrash.models.TeamMember;
import java.util.ArrayList;
import java.util.List;

public class TeamFragment extends Fragment {
    private RecyclerView teamRecyclerView;
    private TeamAdapter teamAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_team, container, false);
        
        teamRecyclerView = view.findViewById(R.id.teamRecyclerView);
        teamRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        // Create sample team members
        List<TeamMember> teamMembers = new ArrayList<>();
        teamMembers.add(new TeamMember(
            "KALEGGA MUHAMMAD",
            "Project Manager",
            "Oversees project planning, execution, and team coordination",
            R.drawable.med
        ));
        teamMembers.add(new TeamMember(
            "PRISCA PINYOLOYA",
            "Lead Developer",
            "Leads the development team and ensures code quality",
            R.drawable.pinyo
        ));
        teamMembers.add(new TeamMember(
            "NAKALIMO VERONICA",
            "UI/UX Designer",
            "Creates intuitive and beautiful user interfaces",
            R.drawable.veron
        ));
        
        teamAdapter = new TeamAdapter(teamMembers);
        teamRecyclerView.setAdapter(teamAdapter);
        
        return view;
    }
} 