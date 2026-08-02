import React, { useEffect, useState } from 'react';
import { withAuth } from '@components/withAuth.jsx';
import { api } from '@services/apiService.js';
import DataTable from '@components/DataTable.jsx';
import { StatusPill } from '@components/StatusPill.jsx';
import { formatTimestamp } from '@/lib/utils';

const COLUMNS = [
  { key: 'jobId', label: 'Job ID' },
  { key: 'fromDate', label: 'From' },
  { key: 'toDate', label: 'To' },
  { key: 'status', label: 'Status' },
  { key: 'startedAt', label: 'Started At', width: 'minmax(10rem, 1.25fr)' },
  { key: 'finishedAt', label: 'Finished At', width: 'minmax(10rem, 1.25fr)' },
  { key: 'tradesProcessed', label: 'Processed' },
  { key: 'breaksDetected', label: 'Breaks' },
];

function ReconJobs() {
  const [jobs, setJobs] = useState({ items: [], totalPages: 0 });
  const [page, setPage] = useState(0);
  const [uploadMessage, setUploadMessage] = useState(null);
  const [isUploadError, setIsUploadError] = useState(false);
  const [isUploading, setIsUploading] = useState(false);

  useEffect(() => {
    let cancelled = false;
    api.listReconJobs(`?page=${page}&size=20`)
      .then((data) => { if (!cancelled) setJobs(data); })
      .catch(() => { if (!cancelled) setJobs({ items: [], totalPages: 0 }); });
    return () => { cancelled = true; };
  }, [page]);

  const handleFileSubmit = async (event) => {
    event.preventDefault();
    const form = event.currentTarget;

    setUploadMessage(null);
    setIsUploadError(false);
    setIsUploading(true);

    const formData = new FormData(form);
    try {
      await api.runReconCsv(formData);
      setUploadMessage('Recon job queued successfully. The status will appear in the list shortly.');
      setIsUploadError(false);
      if (typeof form.reset === 'function') {
        form.reset();
      }
    } catch (err) {
      setUploadMessage(err.message || 'Unable to submit the recon job. Please try again.');
      setIsUploadError(true);
    } finally {
      setIsUploading(false);
    }
  };

  return (
    <section>
      <h2 className="font-display text-2xl font-semibold text-ink">Recon Jobs</h2>

      <form
        onSubmit={handleFileSubmit}
        className="mt-4 grid max-w-xl gap-4 rounded-xl border border-line bg-paper p-6 shadow-sm sm:grid-cols-2"
      >
        <label className="grid gap-1 text-sm text-ink">
          From date
          <input
            type="date"
            name="from"
            required
            className="rounded-lg border border-line bg-canvas/40 px-3 py-2 text-sm text-ink focus:border-signal focus:outline-none"
          />
        </label>
        <label className="grid gap-1 text-sm text-ink">
          To date
          <input
            type="date"
            name="to"
            required
            className="rounded-lg border border-line bg-canvas/40 px-3 py-2 text-sm text-ink focus:border-signal focus:outline-none"
          />
        </label>
        <label className="grid gap-1 text-sm text-ink sm:col-span-2">
          External trades CSV
          <input
            type="file"
            name="file"
            accept=".csv"
            required
            className="rounded-lg border border-line bg-canvas/40 px-3 py-1.5 text-sm text-ink file:mr-3 file:rounded-md file:border-0 file:bg-signal file:px-3 file:py-1.5 file:text-sm file:font-medium file:text-white file:cursor-pointer"
          />
        </label>
        {uploadMessage && (
          <p
            role="alert"
            className={`text-sm sm:col-span-2 ${isUploadError ? 'text-danger' : 'text-ink'}`}
          >
            {uploadMessage}
          </p>
        )}
        <button
          type="submit"
          disabled={isUploading}
          className="cursor-pointer justify-self-start rounded-lg bg-signal px-4 py-2 text-sm font-medium text-white transition-opacity hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-50 sm:col-span-2"
        >
          {isUploading ? 'Submitting…' : 'Run job'}
        </button>
      </form>

      <div className="mt-6">
        <DataTable columns={COLUMNS}>
          <DataTable.Header columns={COLUMNS} />
          <DataTable.Body rows={jobs.items} render={(job) => (
            <span className="contents">
              <span className="figures text-ink">{job.jobId}</span>
              <span className="figures text-ink">{job.fromDate}</span>
              <span className="figures text-ink">{job.toDate}</span>
              <StatusPill status={job.status} />
              <span className="figures text-ink">{formatTimestamp(job.startedAt)}</span>
              <span className="figures text-ink">{formatTimestamp(job.finishedAt)}</span>
              <span className="figures text-ink">{job.tradesProcessed ?? '-'}</span>
              <span className="figures text-ink">{job.breaksDetected ?? '-'}</span>
            </span>
          )} />
          <DataTable.Pagination
            page={page}
            totalPages={Math.max(1, jobs.totalPages)}
            onChange={setPage}
          />
        </DataTable>
      </div>
    </section>
  );
}

export default withAuth(ReconJobs);
