import { createSlice, type PayloadAction } from '@reduxjs/toolkit'
import type { ConsumptionPeriod } from '@/types/dashboard'

interface DashboardState {
  period: ConsumptionPeriod
  // null means "all sites of the company"
  siteId: string | null
}

const initialState: DashboardState = {
  period: 'TODAY',
  siteId: null,
}

export const dashboardSlice = createSlice({
  name: 'dashboard',
  initialState,
  reducers: {
    setPeriod(state, action: PayloadAction<ConsumptionPeriod>) {
      state.period = action.payload
    },
    setSite(state, action: PayloadAction<string | null>) {
      state.siteId = action.payload
    },
  },
})

export const { setPeriod, setSite } = dashboardSlice.actions