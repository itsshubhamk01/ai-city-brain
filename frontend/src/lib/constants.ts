import type { AgentType, RiskLevel, Role, Severity } from '../types'

export const RISK_COLORS: Record<RiskLevel, string> = {
  LOW: '#3DDC97',
  MODERATE: '#F5A623',
  HIGH: '#FF8A5D',
  CRITICAL: '#FF5D5D',
}

export const SEVERITY_COLORS: Record<Severity, string> = {
  LOW: '#3DDC97',
  MODERATE: '#F5A623',
  HIGH: '#FF8A5D',
  CRITICAL: '#FF5D5D',
}

export const AGENT_COLORS: Record<AgentType, string> = {
  FLOOD: '#22D3EE',
  TRAFFIC: '#F5A623',
  EMERGENCY: '#FF5D5D',
  WASTE: '#A3E635',
  ENERGY: '#C084FC',
  HEALTHCARE: '#FB7185',
  CITY_BRAIN: '#5B8DEF',
}

export const AGENT_LABELS: Record<AgentType, string> = {
  FLOOD: 'Flood Agent',
  TRAFFIC: 'Traffic Agent',
  EMERGENCY: 'Emergency Agent',
  WASTE: 'Waste Agent',
  ENERGY: 'Energy Agent',
  HEALTHCARE: 'Healthcare Agent',
  CITY_BRAIN: 'City Brain',
}

export const ROLE_LABELS: Record<Role, string> = {
  ADMIN: 'Administrator',
  OPERATIONS_MANAGER: 'Operations Manager',
  EMERGENCY_RESPONDER: 'Emergency Responder',
  TRAFFIC_MANAGER: 'Traffic Manager',
  ANALYST: 'Analyst',
  CITIZEN: 'Citizen',
}

export const DEMO_ACCOUNTS: { username: string; password: string; role: Role }[] = [
  { username: 'admin', password: 'admin123', role: 'ADMIN' },
  { username: 'ops_manager', password: 'ops123', role: 'OPERATIONS_MANAGER' },
  { username: 'responder', password: 'responder123', role: 'EMERGENCY_RESPONDER' },
  { username: 'traffic_mgr', password: 'traffic123', role: 'TRAFFIC_MANAGER' },
  { username: 'analyst', password: 'analyst123', role: 'ANALYST' },
  { username: 'citizen', password: 'citizen123', role: 'CITIZEN' },
]

/** Roles allowed to see + use the Simulation Control / What-If / write endpoints. */
export const OPERATOR_ROLES: Role[] = ['ADMIN', 'OPERATIONS_MANAGER']
export const CAN_MANAGE_INCIDENTS: Role[] = ['ADMIN', 'OPERATIONS_MANAGER', 'EMERGENCY_RESPONDER', 'TRAFFIC_MANAGER']
export const CAN_USE_WHATIF: Role[] = ['ADMIN', 'OPERATIONS_MANAGER', 'ANALYST', 'TRAFFIC_MANAGER', 'EMERGENCY_RESPONDER']
