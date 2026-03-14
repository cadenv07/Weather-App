package com.swirlingskies.weatherapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.maplibre.android.camera.CameraPosition;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.geometry.LatLngBounds;
import org.maplibre.android.maps.MapLibreMapOptions;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.Style;
import org.maplibre.android.style.layers.LineLayer;
import org.maplibre.android.style.layers.PropertyFactory;
import org.maplibre.android.style.sources.GeoJsonSource;

import java.net.URI;

public class RadarMapFragment extends Fragment {
    private MapView mapView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mapView = new MapView(requireContext(), MapLibreMapOptions.createFromAttributes(requireContext()));
        return mapView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(map -> {
            // Restrict panning to CONUS + a bit of buffer for Canada/Mexico
            LatLngBounds conusBounds = new LatLngBounds.Builder()
                    .include(new LatLng(49.5, -125.0))  // NW corner
                    .include(new LatLng(24.0, -66.0))   // SE corner
                    .build();

            map.setLatLngBoundsForCameraTarget(conusBounds);
            map.setMinZoomPreference(3.5);  // can't zoom out past CONUS view

            CameraPosition initial = new CameraPosition.Builder()
                    .target(new LatLng(38.5, -98.0))  // center of CONUS
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
        style.addSource(new GeoJsonSource("counties-src",
                URI.create("asset://counties.geojson")));
        LineLayer countiesLayer = new LineLayer("counties-layer", "counties-src");
        countiesLayer.setMinZoom(5f);
        countiesLayer.setProperties(
                PropertyFactory.lineColor("#444444"),
                PropertyFactory.lineWidth(0.5f),
                PropertyFactory.lineOpacity(0.8f)
        );
        style.addLayer(countiesLayer);
//        style.addSource(new GeoJsonSource("states-src",
//                URI.create("asset://states.geojson")));
//        LineLayer statesLayer = new LineLayer("states-layer", "states-src");
//        statesLayer.setProperties(
//                PropertyFactory.lineColor("#AAAAAA"),
//                PropertyFactory.lineWidth(0.5f),
//                PropertyFactory.lineOpacity(0.8f)
//        );
//        style.addLayer(statesLayer);

        style.addSource(new GeoJsonSource("world-src",URI.create("asset://world.geojson")));
        LineLayer worldLayer = new LineLayer("world-layer", "world-src");
        worldLayer.setProperties(
                PropertyFactory.lineColor("#AAAAAA"),
                PropertyFactory.lineWidth(0.5f),
                PropertyFactory.lineOpacity(0.8f)
        );
        style.addLayer(worldLayer);
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}
