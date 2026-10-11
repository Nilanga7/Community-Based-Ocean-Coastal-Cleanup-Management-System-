import React from 'react';
import { StatusBadge } from '../design-system';
import { statusKey } from '../lib/format.js';

// The design system's lifecycle has no "approved" state. A report the government officer approved has
// become a project, so it is shown with the verified styling under that name.
export default function ReportStatusBadge({ status, ...props }) {
  if (status === 'APPROVED') {
    return <StatusBadge status="verified" label="Project" {...props} />;
  }
  return <StatusBadge status={statusKey(status)} {...props} />;
}
