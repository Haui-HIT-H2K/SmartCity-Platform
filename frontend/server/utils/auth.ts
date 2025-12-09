/**
 * Utility function to get auth headers from the request event
 */
import { getCookie } from "h3";
import type { H3Event } from "h3";

export function getAuthHeaders(event: H3Event): Record<string, string> {
  const headers: Record<string, string> = {};
  
  // Get auth token from cookie
  const authToken = getCookie(event, 'auth_token');
  if (authToken) {
    headers['Authorization'] = `Bearer ${authToken}`;
  }
  
  return headers;
}
