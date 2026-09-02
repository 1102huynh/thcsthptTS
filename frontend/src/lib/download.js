/**
 * Triggers a browser download from an axios response promise made with
 * `responseType: 'blob'` (see reportService in dataService.js) — per
 * IMPLEMENTATION_PLAN.md 3.8's PDF/Excel export endpoints, all of which
 * return raw bytes with a Content-Disposition header, not JSON.
 *
 * A blob response has no `.data.message` to read on failure - axios
 * applies the request's `responseType` to error responses too, so a 400/403
 * from these endpoints arrives as `err.response.data` being a Blob, not a
 * parsed object. The usual `err?.response?.data?.message` pattern used
 * everywhere else in this app silently reads `undefined` here - this reads
 * the blob back out as text/JSON first so the real backend message (e.g.
 * "STUDENT/PARENT chỉ xem được của chính mình") still reaches the caller.
 */
export async function triggerBlobDownload(requestPromise, filename) {
  let res;
  try {
    res = await requestPromise;
  } catch (err) {
    let message = err?.message || 'Không thể tải xuống tệp';
    if (err?.response?.data instanceof Blob) {
      try {
        const text = await err.response.data.text();
        message = JSON.parse(text)?.message || message;
      } catch {
        // Not JSON (e.g. a plain-text 500) - keep the generic message.
      }
    }
    throw new Error(message);
  }

  const url = window.URL.createObjectURL(res.data);
  const link = document.createElement('a');
  link.href = url;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  link.remove();
  window.URL.revokeObjectURL(url);
}
