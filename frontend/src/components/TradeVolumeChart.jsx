import React from 'react';
import { ResponsiveLine } from '@nivo/line';

// Sample hardcoded data: daily trades for a few months
const sampleData = [
  {
    id: 'trades',
    color: '#25eb77',
    data: [
      { x: '2026-01-01', y: 12 },
      { x: '2026-01-08', y: 18 },
      { x: '2026-01-15', y: 9 },
      { x: '2026-02-01', y: 20 },
      { x: '2026-02-14', y: 24 },
      { x: '2026-03-01', y: 30 },
      { x: '2026-03-15', y: 22 },
      { x: '2026-04-01', y: 28 },
      { x: '2026-04-15', y: 35 },
      { x: '2026-05-01', y: 31 },
      { x: '2026-05-15', y: 27 },
      { x: '2026-06-01', y: 40 },
      { x: '2026-06-15', y: 45 },
    ],
  },
];

export default function TradeVolumeChart({ data = sampleData, height = 320 }) {
  return (
    <div style={{ height }} className="rounded-xl border border-line bg-paper p-4">
      <h3 className="font-display text-lg font-semibold text-ink mb-2">Trade Volume</h3>
      <div style={{ height: height - 48 }}>
        <ResponsiveLine
          data={data}
          xScale={{ type: 'time', format: '%Y-%m-%d', precision: 'day' }}
          xFormat="time:%Y-%m-%d"
          yScale={{ type: 'linear', min: 'auto', max: 'auto', clamp: 'true', stacked: false, reverse: false }}
          axisTop={null}
          axisRight={null}
          axisBottom={{
            format: '%b',
            tickValues: 'every 1 month',
            legend: '',
            legendOffset: 36,
            legendPosition: 'middle',
          }}
          axisLeft={{
            orient: 'left',
            tickSize: 5,
            tickPadding: 5,
            tickRotation: 0,
            legend: 'trades',
            legendOffset: -40,
            legendPosition: 'middle',
          }}
          colors={[ '#65f3b3' ]}
          pointSize={6}
          pointColor={{ theme: 'background' }}
          pointBorderWidth={2}
          pointBorderColor={{ from: 'serieColor' }}
          useMesh={true}
          enableArea={true}
          areaOpacity={0.10}
          curve="monotoneX"
          margin={{ top: 10, right: 30, bottom: 50, left: 60 }}
          enableGridX={false}
          enableGridY={true}
          legends={[]}
          animate={true}
          tooltip={({ point }) => (
            <div style={{ background: '#0b1220', color: '#fff', padding: 8, borderRadius: 6 }}>
              <div style={{ fontSize: 12, opacity: 0.9 }}>{point.data.xFormatted} {point.data.y} Trades</div>
            </div>
          )}
          theme={{
            tooltip: { container: { background: '#0e200b', color: '#fff' } },
            axis: { ticks: { text: { fill: '#6b7280' } }, legend: { text: { fill: '#374151' } } },
            grid: { line: { stroke: '#e6e9efb0' } },
          }}
        />
      </div>
    </div>
  );
}
