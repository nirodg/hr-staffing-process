export const ROLES: Record<string, string> = {
    CLIENT_PUBLIC_ADMIN: "Admin",
    CLIENT_PUBLIC_USER: "User",
};

export function getRoleFromEnum(role: string) {
    return ROLES[String(role)];
}
