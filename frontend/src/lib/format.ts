// Turkish number formats for the dashboard: 1.240, 7.380, 0,474
export const formatNumber = (value: number, fractionDigits = 0) =>
  value.toLocaleString('tr-TR', { minimumFractionDigits: fractionDigits, maximumFractionDigits: fractionDigits })