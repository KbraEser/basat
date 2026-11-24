// Mirrors ConsumptionPeriod in monitoring-service: every period starts at local midnight and runs until now.
export type ConsumptionPeriod = 'TODAY' | 'LAST_7_DAYS' | 'LAST_30_DAYS'