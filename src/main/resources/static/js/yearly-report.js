/**
 * @author Rakhi Sharma
 * @description JavaScript functionality to load yearly report on the selected year basis.
 */

(function(){
	const REPORT_API_URL = "/api/reports/yearly";

	const CURRENCY = new Intl.NumberFormat("en-IN", {
		style: "currency",
		currency: "INR",
		maximumFractionDigits: 2
	});

	const MONTHS = [
		"January", "February", "March", "April",
		"May", "June", "July", "August",
		"September", "October", "November", "December"
	];

	const PALETTE = [
		"#3478f6", "#ff9f1c", "#2fb875", "#9856df",
		"#f05268", "#f5bd28", "#12b8b0", "#8b9bb4",
		"#ed6b35", "#597ef7"
	];

	const yearSelect = document.getElementById("yearSelect");
	const message = document.getElementById("reportMessage");
	const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
	const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

	const charts = {};

	const money = value => CURRENCY.format(Number(value) || 0);
	const sum = items => items.reduce((total, item) => total + item.amount, 0);
	// Normalize API data into a consistent format.
	function normalizeExpenses(rows) {
	    return rows.map(item => {
	        const dateValue = item.date ?? item.expenseDate;
	        const date = dateValue ? new Date(dateValue) : null;

	        return {
	            id: item.id ?? item.expenseId ?? "",
	            title: item.title ?? item.expenseTitle ?? "Expense",
	            amount: Number(item.amount) || 0,
	            date: date && !Number.isNaN(date.getTime()) ? date : null,
	            category: item.category ?? item.categoryName ?? "Uncategorized",
	            subcategory: item.subcategory ?? item.subCategoryName ?? "Other",
	            payment: String(item.paymentMethod ?? "Other").replaceAll("_", " ")
	        };
	    }).filter(item => item.date && item.amount >= 0);
	}
	// Calculate totals grouped by category, subcategory, or payment method.
	function groupTotal(items, keyFn) {
		const totals = new Map();

		items.forEach(item => {
			const key = keyFn(item);
			totals.set(key, (totals.get(key) || 0) + item.amount);
		});

		return [...totals.entries()]
			.map(([label, amount]) => ({ label, amount }))
			.sort((a, b) => b.amount - a.amount);
	}

	function destroyChart(name) {
		if (charts[name]) {
			charts[name].destroy();
		}
	}
	// Create a doughnut chart.
	function createDonut(canvasId, chartName, items) {
		destroyChart(chartName);
		const canvas = document.getElementById(canvasId);
		if (!canvas) return;

		const labels = items.map(item => item.label);
		const values = items.map(item => item.amount);

		charts[chartName] = new Chart(canvas, {
			type: "doughnut",
			data: {
				labels: labels,
				datasets: [{
					data: values,
					backgroundColor: labels.map(
						(_, index) => PALETTE[index % PALETTE.length]
					),
					borderColor: "#ffffff",
					borderWidth: 2,
					hoverOffset: 5
				}]
			},
			options: {
				responsive: true,
				maintainAspectRatio: false,
				cutout: "58%",
				plugins: {
					legend: { display: false },
					tooltip: {
						callbacks: {
							label: context =>
								`${context.label}: ${money(context.raw)}`
						}
					}
				}
			}
		});
	}
	// Prevent HTML content from API values being interpreted as markup.
	function escapeHtml(value) {
		return String(value ?? "").replace(/[&<>"']/g, character => ({
			"&": "&amp;",
			"<": "&lt;",
			">": "&gt;",
			'"': "&quot;",
			"'": "&#39;"
		})[character]);
	}
	// Display chart legends with amounts and percentages.
	function renderLegend(elementId, items, total) {
		const element = document.getElementById(elementId);
		if (!element) return;

		if (!items.length || total === 0) {
			element.innerHTML = '<p class="legend-empty">No expense data</p>';
			return;
		}
		element.innerHTML = items.slice(0, 7).map((item, index) => {
			const percent = (item.amount / total) * 100;

			return `<div class="legend-row">
	                    <span class="legend-name">
	                        <i style="--legend-color:${PALETTE[index % PALETTE.length]}"></i>
	                        <span title="${escapeHtml(item.label)}">
	                            ${escapeHtml(item.label)}
	                        </span>
	                    </span>
	                    <strong>${money(item.amount)}</strong>
	                    <small>${percent.toFixed(1)}%</small>
	                </div>`;
		}).join("");
	}
	// Display the total expenses for each month.
	function renderMonthlyChart(expenses, year) {
		const totals = Array(12).fill(0);
		expenses.forEach(item => {
			if (item.date.getFullYear() === year) {
				totals[item.date.getMonth()] += item.amount;
			}
		});

		destroyChart("monthly");

		const canvas = document.getElementById("yearlyMonthlyChart");
		if (!canvas) return totals;
		charts.monthly = new Chart(canvas, {
			type: "bar",
			data: {
				labels: MONTHS.map(month => month.slice(0, 3)),
				datasets: [{
					label: "Expenses",
					data: totals,
					backgroundColor: "#3478f6",
					borderRadius: 5,
					maxBarThickness: 30
				}]
			},
			options: {
				responsive: true,
				maintainAspectRatio: false,
				scales: {
					y: {
						beginAtZero: true,
						ticks: {
							callback: value => money(value)
						},
						grid: { color: "#e8eef7" }
					},
					x: {
						grid: { display: false }
					}
				},
				plugins: {
					legend: { display: false },
					tooltip: {
						callbacks: {
							label: context => money(context.raw)
						}
					}
				}
			}
		});

		return totals;
	}
	// Display the ten most recent expenses.
	function renderRecentExpenses(expenses) {
		const body = document.getElementById("recentExpensesBody");
		if (!body) return;
		const latest = [...expenses].sort((a, b) => b.date - a.date).slice(0, 12);
		if (!latest.length) {
			body.innerHTML = `
	                <tr>
	                    <td colspan="6" class="empty-state">
	                        No active expenses found for this year.
	                    </td>
	                </tr>`;
			return;
		}
		body.innerHTML = latest.map(item => `
	            <tr>
	                <td>${escapeHtml(item.date.toLocaleDateString("en-CA"))}</td>
	                <td class="expense-title-cell">${escapeHtml(item.title)}</td>
	                <td>${escapeHtml(item.category)}</td>
	                <td>${escapeHtml(item.subcategory)}</td>
	                <td>
	                    <span class="payment-pill">
	                        ${escapeHtml(item.payment)}
	                    </span>
	                </td>
	                <td class="text-end amount-cell">${money(item.amount)}</td>
	            </tr>
	        `).join("");
	}
	// Update summary cards and charts.
	function renderReport(rows, year) {
		const expenses = normalizeExpenses(rows).filter(
			item => item.date.getFullYear() === year
		);

		const total = sum(expenses);
		const monthlyTotals = renderMonthlyChart(expenses, year);
		const categoryTotals = groupTotal(expenses, item => item.category);
		const subcategoryTotals = groupTotal(expenses, item => item.subcategory);
		const paymentTotals = groupTotal(expenses, item => item.payment);
		const highestMonthIndex = monthlyTotals.reduce((best, amount, index, array) => amount > array[best] ? index : best, 0);
		document.getElementById("totalExpenses").textContent = money(total);
		document.getElementById("totalYearLabel").textContent = `For ${year}`;
		document.getElementById("highestMonth").textContent = total > 0 ? MONTHS[highestMonthIndex] : "—";
		document.getElementById("highestMonthAmount").textContent = money(total > 0 ? monthlyTotals[highestMonthIndex] : 0);
		document.getElementById("topCategory").textContent = categoryTotals.length ? categoryTotals[0].label : "—";
		document.getElementById("topCategoryAmount").textContent = money(categoryTotals[0]?.amount || 0);
		document.getElementById("topPaymentMethod").textContent = paymentTotals.length ? paymentTotals[0].label : "—";
		document.getElementById("topPaymentAmount").textContent = money(paymentTotals[0]?.amount || 0);
		document.getElementById("monthlyTrendSubtitle").textContent = `Total expense for each month in ${year}`;
		createDonut("yearlyCategoryChart", "category", categoryTotals);
		createDonut("yearlySubcategoryChart", "subcategory", subcategoryTotals);
		createDonut("yearlyPaymentChart", "payment", paymentTotals);
		renderLegend("categoryLegend", categoryTotals, total);
		renderLegend("subcategoryLegend", subcategoryTotals, total);
		renderLegend("paymentLegend", paymentTotals, total);

		renderRecentExpenses(expenses);
	}
	function initializeYearlyReport() {
		const year = Number(yearSelect.value);
		if (!year) {
			message.textContent = "Please select a valid year.";
			message.className = "report-message is-visible is-error";
			return;
		}
		message.textContent = "Loading annual report…";
		message.className = "report-message is-visible";
		const headers = {};

		if (csrfToken && csrfHeader) {
			headers[csrfHeader] = csrfToken;
		}
		const url = new URL(REPORT_API_URL, window.location.href);
		url.searchParams.set("year", year);
		fetch(url, {
		    method: "GET",
		    headers: headers
		})
		.then(response => {
		    if (!response.ok) {
		        throw new Error(
		            `Unable to load the annual report. HTTP status: ${response.status}`
		        );
		    }

		    return response.json();
		})
		.then(result => {
		    if (result.status !== "OK") {
		        throw new Error(
		            result.responseMessage || "Unable to load the annual report."
		        );
		    }

		    const rows = Array.isArray(result.response) ? result.response : [];
		    renderReport(rows, year);

		    message.textContent = rows.length
		        ? `Report loaded for ${year}.`
		        : `No expense records returned for ${year}.`;

		    message.className = "report-message is-visible is-success";
		})
		.catch(error => {
		    console.error("Yearly report error:", error);
		    message.textContent = error.message || "Unable to load the yearly report.";
		    message.className = "report-message is-visible is-error";

		    renderReport([], year);
		});
	}
	// Initialize the report when the year changes or the page loads.
	yearSelect.addEventListener("change", initializeYearlyReport);
	initializeYearlyReport();
})();