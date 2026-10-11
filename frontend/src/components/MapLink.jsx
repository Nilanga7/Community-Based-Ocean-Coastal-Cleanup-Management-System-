import React from 'react';
import { Button } from '../design-system';

/** Module 6 asks for alerts "with map location" — this opens the spot in OpenStreetMap. */
export default function MapLink({ latitude, longitude, label = 'Open in map' }) {
  if (latitude == null || longitude == null) return null;
  const href = `https://www.openstreetmap.org/?mlat=${latitude}&mlon=${longitude}#map=16/${latitude}/${longitude}`;
  return (
    <Button as="a" href={href} target="_blank" rel="noopener noreferrer" variant="secondary" size="sm" iconLeft="map">
      {label}
    </Button>
  );
}
