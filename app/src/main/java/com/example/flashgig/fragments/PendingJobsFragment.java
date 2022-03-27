package com.example.flashgig.fragments;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.flashgig.R;
import com.example.flashgig.activities.JobRecyclerViewAdapter;
import com.example.flashgig.databinding.FragmentPendingJobsBinding;
import com.example.flashgig.models.Job;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;

public class PendingJobsFragment extends Fragment implements JobRecyclerViewAdapter.ItemClickListener {
    private FirebaseFirestore db;
    private ArrayList<Job> jobList = new ArrayList<>();
    private JobRecyclerViewAdapter adapter;
    String curUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        curUser = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        eventChangeListener();
    }

    private void eventChangeListener() {
        db.collection("jobs").whereEqualTo("client",curUser).whereEqualTo("status","pending").orderBy("timestamp", Query.Direction.DESCENDING).addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.d("error", "Firebase error");
            }
            else{
                for (DocumentChange dc : value.getDocumentChanges()) {
                    if(dc.getType() == DocumentChange.Type.ADDED){
                        jobList.add(dc.getDocument().toObject(Job.class));
                    }
                    else if(dc.getType() == DocumentChange.Type.REMOVED){
                        jobList.remove(dc.getDocument().toObject(Job.class));
                    }
                    else{
                        jobList.add(dc.getDocument().toObject(Job.class));
                        jobList.remove(dc.getDocument().toObject(Job.class));
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        FragmentPendingJobsBinding binding = FragmentPendingJobsBinding.inflate(inflater, container, false);

        RecyclerView recyclerView = binding.recyclerViewPendingJobs;
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));

        adapter = new JobRecyclerViewAdapter(this.getContext(), jobList, this);
        recyclerView.setAdapter(adapter);

        // Inflate the layout for this fragment
        return binding.getRoot();
    }

    @Override
    public void onItemClick(String JID) {
        Fragment fragment = DetailFragment.newInstance(JID);
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment, "jobDetail");
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}