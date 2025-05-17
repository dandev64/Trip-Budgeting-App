package muli.labs;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmResults;

public class TripAnalysisFragment extends Fragment {

    private Realm realm;
    private String tripId;
    private BarChart contributionBarChart;
    private PieChart contributionPieChart;
    private Spinner tripSpinner;

    private static final int[] PASTEL_COLORS = {
            Color.rgb(255, 179, 186),
            Color.rgb(255, 223, 186),
            Color.rgb(255, 255, 186),
            Color.rgb(186, 255, 201),
            Color.rgb(186, 225, 255)
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trip_analysis, container, false);

        contributionBarChart = view.findViewById(R.id.contributionBarChart);
        contributionPieChart = view.findViewById(R.id.contributionPieChart);
        tripSpinner = view.findViewById(R.id.tripSpinner);

        realm = Realm.getDefaultInstance();

        setupTripSpinner();

        return view;
    }

    private void setupTripSpinner() {
        RealmResults<Trip> trips = realm.where(Trip.class).findAll();
        List<String> tripNames = new ArrayList<>();
        final List<String> tripIds = new ArrayList<>();

        for (Trip trip : trips) {
            tripNames.add(trip.getName());
            tripIds.add(trip.getId());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, tripNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tripSpinner.setAdapter(adapter);

        tripSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                tripId = tripIds.get(position);
                displayContributionBarChart();
                displayContributionPieChart();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle case where no trip is selected if needed
            }
        });
    }

    private void displayContributionBarChart() {
        RealmResults<Transaction> transactions = realm.where(Transaction.class).equalTo("tripId", tripId).findAll();
        Map<String, Double> balanceMap = new HashMap<>();

        for (Transaction transaction : transactions) {
            for (Contributor contributor : transaction.getContributors()) {
                String friendId = contributor.getFriendId();
                double amountOwed = contributor.getAmountOwed();
                double contributedAmount = contributor.getContributedAmount();

                double balance = contributedAmount - amountOwed;

                balanceMap.put(friendId, balanceMap.getOrDefault(friendId, 0.0) + balance);
            }
        }

        List<BarEntry> entries = new ArrayList<>();
        List<String> friendNames = new ArrayList<>();
        int i = 0;

        for (Map.Entry<String, Double> entry : balanceMap.entrySet()) {
            String friendId = entry.getKey();
            double balance = entry.getValue();

            Friend friend = realm.where(Friend.class).equalTo("id", friendId).findFirst();
            if (friend != null) {
                entries.add(new BarEntry(i++, (float) balance));
                friendNames.add(friend.getName());
            }
        }

        BarDataSet dataSet = new BarDataSet(entries, "Final Balance");
        dataSet.setColors(PASTEL_COLORS);
        BarData barData = new BarData(dataSet);
        barData.setValueTextSize(14f);

        contributionBarChart.setData(barData);

        XAxis xAxis = contributionBarChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(friendNames));
        xAxis.setGranularity(1f);
        xAxis.setTextSize(14f);
        xAxis.setGranularityEnabled(true);

        // Set custom legend
        Legend legend = contributionBarChart.getLegend();
        LegendEntry[] legendEntries = new LegendEntry[friendNames.size()];
        for (int j = 0; j < friendNames.size(); j++) {
            LegendEntry entry = new LegendEntry();
            entry.label = friendNames.get(j);
            entry.formColor = PASTEL_COLORS[j % PASTEL_COLORS.length];
            legendEntries[j] = entry;
        }
        legend.setCustom(legendEntries);

        contributionBarChart.getDescription().setEnabled(false); // Remove description label
        contributionBarChart.invalidate();
    }

    private void displayContributionPieChart() {
        RealmResults<Transaction> transactions = realm.where(Transaction.class).equalTo("tripId", tripId).findAll();
        Map<String, Double> totalContributionMap = new HashMap<>();

        for (Transaction transaction : transactions) {
            for (Contributor contributor : transaction.getContributors()) {
                String friendId = contributor.getFriendId();
                double contributedAmount = contributor.getContributedAmount();

                totalContributionMap.put(friendId, totalContributionMap.getOrDefault(friendId, 0.0) + contributedAmount);
            }
        }

        List<PieEntry> entries = new ArrayList<>();

        for (Map.Entry<String, Double> entry : totalContributionMap.entrySet()) {
            String friendId = entry.getKey();
            double totalContribution = entry.getValue();

            if (totalContribution > 0) {
                Friend friend = realm.where(Friend.class).equalTo("id", friendId).findFirst();
                if (friend != null) {
                    entries.add(new PieEntry((float) totalContribution, friend.getName()));
                }
            }
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(PASTEL_COLORS);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(14f);

        PieData pieData = new PieData(dataSet);
        pieData.setValueTextSize(14f);
        pieData.setValueTextColor(Color.BLACK);

        contributionPieChart.setData(pieData);

        contributionPieChart.setEntryLabelColor(Color.BLACK);
        contributionPieChart.setEntryLabelTextSize(12f);

        contributionPieChart.getLegend().setEnabled(false);
        contributionPieChart.getDescription().setEnabled(false);
        contributionPieChart.invalidate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
