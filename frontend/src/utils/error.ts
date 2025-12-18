export function extractErrorMessage(error: unknown, fallback = ''): string {
  const typedError = error as { response?: { data?: { message?: unknown } }; message?: unknown } | undefined;
  const backendMessage = typedError?.response?.data?.message;
  if (typeof backendMessage === 'string' && backendMessage.trim()) {
    return backendMessage.trim();
  }
  const message = typedError?.message;
  if (typeof message === 'string' && message.trim()) {
    return message.trim();
  }
  return fallback;
}

export function buildDisplayError(prefix: string | undefined, error: unknown, fallback?: string): string | undefined {
  const detail = extractErrorMessage(error, fallback);
  if (detail) {
    return prefix ? `${prefix}：${detail}` : detail;
  }
  return prefix;
}
