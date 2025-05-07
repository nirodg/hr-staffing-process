// core/constants/status-map.ts
export const STATUS_BY_ACTIVE: Record<'true' | 'false', { label: string; color: string; icon: string }> = {
    true: { label: 'Completed', color: 'bg-green-500', icon: '✅' },
    false: { label: 'In Progress', color: 'bg-yellow-400', icon: '⏳' },
};

export function getStatusFromActive(isActive: boolean) {
    return STATUS_BY_ACTIVE[String(isActive) as 'true' | 'false'];
}
