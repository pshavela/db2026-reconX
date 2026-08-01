// TICKET-ADV123 — React Hook Form + Yup validation.
import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import { withAuth } from '@components/withAuth.jsx';
import { api } from '@services/apiService.js';

// tradeDate is kept as a plain string (not yup.date()) — the <input type="date">
// already gives "YYYY-MM-DD", exactly what the backend's LocalDate field expects.
// Casting it to a JS Date and JSON.stringify-ing it would send a full ISO
// datetime instead, which the backend can't parse as a LocalDate.
const schema = yup.object({
  tradeRef: yup.string()
    .matches(/^[A-Z]{3}-\d{8}-\d{4}$/, 'Format: AAA-YYYYMMDD-NNNN')
    .required('Trade ref is required'),
  instrumentId: yup.number().typeError('Must be a number').integer().positive().required('Instrument id is required'),
  counterpartyId: yup.number().typeError('Must be a number').integer().positive().required('Counterparty id is required'),
  assetClass: yup.string().oneOf(['EQUITY', 'FX', 'BOND', 'DERIVATIVE']).required(),
  side: yup.string().oneOf(['BUY', 'SELL']).required(),
  quantity: yup.number().typeError('Must be a number').positive().required('Quantity is required'),
  price: yup.number().typeError('Must be a number').positive().required('Price is required'),
  tradeDate: yup.string().required('Trade date is required'),
});

const inputClass =
  'rounded-lg border border-line bg-canvas/40 px-3 py-2 text-sm text-ink focus:border-signal focus:outline-none';

function FieldError({ message }) {
  if (!message) return null;
  return <p role="alert" className="text-xs text-danger">{message}</p>;
}

function AddTrade() {
  const [submitError, setSubmitError] = useState(null);
  const { register, handleSubmit, formState: { errors, isSubmitting }, reset } =
        useForm({
          resolver: yupResolver(schema),
          mode: 'onBlur',
          defaultValues: {
            tradeRef: '', instrumentId: '', counterpartyId: '', assetClass: 'EQUITY',
            side: 'BUY', quantity: '', price: '', tradeDate: '',
          },
        });

  async function onSubmit(values) {
    setSubmitError(null);
    try {
      await api.createTrade(values);
      reset();
    } catch (err) {
      setSubmitError(err.message);
    }
  }

  return (
    <section>
      <h2 className="font-display text-2xl font-semibold text-ink">Add trade</h2>
      <form
        onSubmit={handleSubmit(onSubmit)}
        className="mt-4 grid max-w-2xl grid-cols-1 md:grid-cols-2 gap-4 rounded-xl border border-line bg-paper p-6 shadow-sm"
        noValidate
      >
        <div className="space-y-4">
          <label className="grid gap-1 text-sm text-ink">
            Trade ref
            <input {...register('tradeRef')} placeholder="EQU-20260603-0001" className={inputClass} />
          </label>
          <FieldError message={errors.tradeRef?.message} />

          <label className="grid gap-1 text-sm text-ink">
            Instrument id
            <input type="number" {...register('instrumentId')} className={inputClass} />
          </label>
          <FieldError message={errors.instrumentId?.message} />

          <label className="grid gap-1 text-sm text-ink">
            Counterparty id
            <input type="number" {...register('counterpartyId')} className={inputClass} />
          </label>
          <FieldError message={errors.counterpartyId?.message} />

          <label className="grid gap-1 text-sm text-ink">
            Asset class
            <select {...register('assetClass')} className={inputClass}>
              <option value="EQUITY">EQUITY</option>
              <option value="FX">FX</option>
              <option value="BOND">BOND</option>
              <option value="DERIVATIVE">DERIVATIVE</option>
            </select>
          </label>
        </div>

        <div className="space-y-4">
          <label className="grid gap-1 text-sm text-ink">
            Side
            <select {...register('side')} className={inputClass}>
              <option value="BUY">BUY</option>
              <option value="SELL">SELL</option>
            </select>
          </label>

          <label className="grid gap-1 text-sm text-ink">
            Quantity
            <input type="number" step="0.0001" {...register('quantity')} className={inputClass} />
          </label>
          <FieldError message={errors.quantity?.message} />

          <label className="grid gap-1 text-sm text-ink">
            Price
            <input type="number" step="0.0001" {...register('price')} className={inputClass} />
          </label>
          <FieldError message={errors.price?.message} />

          <label className="grid gap-1 text-sm text-ink">
            Trade date
            <input type="date" {...register('tradeDate')} className={inputClass} />
          </label>
          <FieldError message={errors.tradeDate?.message} />

          <FieldError message={submitError} />
        </div>

        <div className="md:col-span-2">
          <button
            disabled={isSubmitting}
            type="submit"
            className="cursor-pointer rounded-lg bg-signal px-4 py-2 text-sm font-medium text-white transition-opacity hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-50"
          >
            Submit
          </button>
        </div>
      </form>
    </section>
  );
}

export default withAuth(AddTrade);
