package com.swirlingskies.weatherapp.fragments;

import static androidx.core.content.ContentProviderCompat.requireContext;

import static org.maplibre.android.style.expressions.Expression.get;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.maplibre.android.camera.CameraPosition;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.geometry.LatLngBounds;
import org.maplibre.android.maps.MapLibreMapOptions;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.Style;
import org.maplibre.android.style.layers.FillLayer;
import org.maplibre.android.style.layers.LineLayer;
import org.maplibre.android.style.layers.PropertyFactory;
import org.maplibre.android.style.sources.GeoJsonSource;

import java.net.URI;

public class MapFragment extends Fragment {

    private MapView mapView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mapView = new MapView(requireContext(), MapLibreMapOptions.createFromAttributes(requireContext()));
        return mapView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(map -> {
            map.getUiSettings().setAttributionEnabled(false);
            map.getUiSettings().setLogoEnabled(false);
            LatLngBounds conusBounds = new LatLngBounds.Builder()
                    .include(new LatLng(49.5, -125.0))
                    .include(new LatLng(24.0, -66.0))
                    .build();

            map.setLatLngBoundsForCameraTarget(conusBounds);
            map.setMinZoomPreference(3.5);

            CameraPosition initial = new CameraPosition.Builder()
                    .target(new LatLng(38.5, -98.0))
                    .zoom(3.5)
                    .build();

            map.setCameraPosition(initial);
            map.setStyle(buildStyle(), this::addAwipsLayers);
        });
    }

    private Style.Builder buildStyle() {
        return new Style.Builder().fromUri("asset://dark.json");
    }

    private void addAwipsLayers(Style style) {
        style.addSource(new GeoJsonSource("counties-src", URI.create("asset://counties.geojson")));
        LineLayer countiesLayer = new LineLayer("counties-layer", "counties-src");
        countiesLayer.setMinZoom(5f);
        countiesLayer.setProperties(
                PropertyFactory.lineColor("#444444"),
                PropertyFactory.lineWidth(0.5f),
                PropertyFactory.lineOpacity(0.8f)
        );
        style.addLayer(countiesLayer);

        style.addSource(new GeoJsonSource("world-src", URI.create("asset://world.geojson")));
        LineLayer worldLayer = new LineLayer("world-layer", "world-src");
        worldLayer.setProperties(
                PropertyFactory.lineColor("#AAAAAA"),
                PropertyFactory.lineWidth(0.5f),
                PropertyFactory.lineOpacity(0.8f)
        );
        style.addLayer(worldLayer);

        RequestQueue queue = Volley.newRequestQueue(requireContext());
        StringRequest stringRequest = new StringRequest(Request.Method.GET, "http://awips-server:8080/spc/9",
                response -> {
                    style.addSource(new GeoJsonSource("spc-outlook-source", response));
                    FillLayer spcFill = new FillLayer("spc-outlook-fill", "spc-outlook-source");
                    spcFill.setProperties(
                            PropertyFactory.fillColor(get("fill")),
                            PropertyFactory.fillOpacity(0.4f)
                    );
                    style.addLayer(spcFill);
                    LineLayer spcLine = new LineLayer("spc-outlook-line", "spc-outlook-source");
                    spcLine.setProperties(
                            PropertyFactory.lineColor(get("stroke")),
                            PropertyFactory.lineWidth(1.5f)
                    );
                    style.addLayer(spcLine);
                },
                error -> Log.e("Volley", "Error: " + error.toString())
        );
        queue.add(stringRequest);
    }

    @Override
    public void onStart() { super.onStart(); mapView.onStart(); }

    @Override
    public void onResume() { super.onResume(); mapView.onResume(); }

    @Override
    public void onPause() { super.onPause(); mapView.onPause(); }

    @Override
    public void onStop() { super.onStop(); mapView.onStop(); }

    @Override
    public void onDestroyView() { super.onDestroyView(); mapView.onDestroy(); }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}