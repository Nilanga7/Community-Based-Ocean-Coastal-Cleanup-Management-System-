import React from 'react';
import { Dialog } from '../design-system';

// Dialog positions itself absolutely against its container; on a scrolled page that is the top
// of the document, off-screen. The fixed wrapper makes the viewport its container instead.
export default function Modal({ open, ...props }) {
  if (!open) return null;
  return (
    <div style={{ position: 'fixed', inset: 0, zIndex: 50 }}>
      <Dialog open {...props} />
    </div>
  );
}
