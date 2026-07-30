import React, { useEffect, useState } from 'react';
import { withAuth } from '@components/withAuth.jsx';
import { api } from '@services/apiService.js';
import DataTable from '@components/DataTable.jsx';

function ReconJobs() {
  const [jobs, setJobs] = useState({ items: [], totalPages: 0 });
  const [page, setPage] = useState(0);
  const [uploadError, setUploadError] = useState(null);
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
    setUploadError(null);
    setIsUploading(true);

    const formData = new FormData(event.currentTarget);
    try {
      await api.runReconCsv(formData);
      setUploadError('Recon job submitted successfully. Refresh list after a moment.');
      event.currentTarget.reset();
    } catch (err) {
      setUploadError(err.message);
    } finally {
      setIsUploading(false);
    }
  };

  return (
    <section>
      <h2>Recon jobs</h2>
      <form onSubmit={handleFileSubmit} className="recon-job-form">
        <label>
          From date
          <input type="date" name="from" required />
        </label>
        <label>
          To date
          <input type="date" name="to" required />
        </label>
        <label>
          External trades CSV
          <input type="file" name="file" accept=".csv" required />
        </label>
        {uploadError && <p role="alert" className="form-error">{uploadError}</p>}
        <button type="submit" disabled={isUploading}>{isUploading ? 'Submitting…' : 'Run job'}</button>
      </form>

      <DataTable>
        <DataTable.Header columns={[
          { key: 'jobId', label: 'Job ID' },
          { key: 'fromDate', label: 'From' },
          { key: 'toDate', label: 'To' },
          { key: 'status', label: 'Status' },
          { key: 'tradesProcessed', label: 'Processed' },
          { key: 'breaksDetected', label: 'Breaks' },
        ]} />
        <DataTable.Body rows={jobs.items} render={(job) => (
          <span style={{ display: 'contents' }}>
            <span>{job.jobId}</span>
            <span>{job.fromDate}</span>
            <span>{job.toDate}</span>
            <span>{job.status}</span>
            <span>{job.tradesProcessed ?? '-'}</span>
            <span>{job.breaksDetected ?? '-'}</span>
          </span>
        )} />
        <DataTable.Pagination
          page={page}
          totalPages={Math.max(1, jobs.totalPages)}
          onChange={setPage}
        />
      </DataTable>
    </section>
  );
}

export default withAuth(ReconJobs);
