document.addEventListener("DOMContentLoaded", () => {
  const notes = document.querySelector("[data-notes]");
  const counter = document.querySelector("[data-notes-counter]");

  if (notes && counter) {
    const updateCounter = () => {
      counter.textContent = String(notes.value.length);
    };
    notes.addEventListener("input", updateCounter);
    updateCounter();
  }

  const select = document.querySelector("[data-tournament-select]");
  const title = document.querySelector("[data-summary-title]");
  const place = document.querySelector("[data-summary-place]");
  const date = document.querySelector("[data-summary-date]");
  const count = document.querySelector("[data-summary-count]");

  if (select && title && place && date && count) {
    const updateSummary = () => {
      const option = select.options[select.selectedIndex];
      title.textContent = option.dataset.title || "";
      place.textContent = `${option.dataset.city || ""}, ${option.dataset.location || ""}`;
      date.textContent = option.dataset.date || "";
      const max = option.dataset.max ? ` / ${option.dataset.max}` : "";
      count.textContent = `Zgłoszenia: ${option.dataset.count || "0"}${max}`;
    };
    select.addEventListener("change", updateSummary);
    updateSummary();
  }
});
