/**
 * @author Rakhi Sharma
 * @description Javascript function to display monthly report graphically
 */

(function($) {
	'use strict';
	const API_URL = '/api/reports/monthly'; 
	const normalize = (e) => ({
		id: e.id,
		title: e.title ?? e.name ?? e.description ?? 'Untitled',
		date: String(e.date ?? e.expenseDate ?? '').slice(0, 10), // YYYY-MM-DD
		category: e.category?.name ?? e.category ?? 'Uncategorized',
		paymentMethod: e.paymentMethod?.name ?? e.paymentMethod ?? 'Unknown',
		amount: Number(e.amount) || 0
	});
	// ==================================================

	const MONTHS = ['January', 'February', 'March', 'April', 'May', 'June', 'July',
		'August', 'September', 'October', 'November', 'December'];
	const PAY_COLORS = ['#4f46e5', '#818cf8', '#c7cbf5', '#e8590c', '#0a7a4b'];

	const money = (n, d = 2) => '₹' + Number(n).toLocaleString('en-IN',
		{ minimumFractionDigits: d, maximumFractionDigits: d });
	const esc = (s) => String(s).replace(/[&<>"']/g,
		(c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
	const parseDate = (d) => new Date(d + 'T00:00:00');
	const dayMonth = (d) => parseDate(d).toLocaleDateString('en-IN', { day: '2-digit', month: 'short' });

	const now = new Date();
	const state = { month: now.getMonth() + 1, year: now.getFullYear() };
	let expenses = [];
	let chart = null;
	let reqId = 0;

	function groupSum(list, keyFn) {
		const map = {};
		list.forEach((e) => { const k = keyFn(e); map[k] = (map[k] || 0) + e.amount; });
		return Object.entries(map).sort((a, b) => b[1] - a[1]);
	}

	function fetchMonth(month, year) {
		return Promise.resolve($.getJSON(API_URL, { month, year }))
			.then((res) => {
				if (res.statusCode !== 1) throw { 
					status: res.responseMessage || 'error' 
				};
				return (res.response || []).map(normalize);
			});
	}

	// ---------- load ----------
	function load() {
		const id = ++reqId;
		const prev = state.month === 1
			? { month: 12, year: state.year - 1 }
			: { month: state.month - 1, year: state.year };

		$('#monthLabel').text(`${MONTHS[state.month - 1]} ${state.year}`);
		$('#nextMonth').prop('disabled',
			state.year > now.getFullYear() ||
			(state.year === now.getFullYear() && state.month >= now.getMonth() + 1));
		$('#reportStatus').addClass('d-none');

		fetchMonth(state.month, state.year)
			.then((list) => {
				if (id === reqId) { 
					expenses = list; 
					render(null); 
				} 
			})
			.catch((xhr) => {
				if (id !== reqId) return;
				expenses = [];
				render(null);
				$('#reportStatus').removeClass('d-none')
					.text(`Could not load the report (${xhr && xhr.status ? xhr.status : 'network error'}).`);
			})
			.then(() => fetchMonth(prev.month, prev.year).catch(() => null))
			.then((prevList) => {
				if (id !== reqId || prevList === null || prevList === undefined) return;
				renderDelta(prevList.reduce((s, e) => s + e.amount, 0));
			});
	}

	function render() {
		renderKpis();
		renderDailyChart();
		renderCategories();
		renderPayments();
		renderTable();
	}

	// ---------- KPIs ----------
	function renderKpis() {
		const total = expenses.reduce((s, e) => s + e.amount, 0);
		const days = new Date(state.year, state.month, 0).getDate();
		const cats = groupSum(expenses, (e) => e.category);
		const byDay = groupSum(expenses, (e) => e.date);

		$('#totalExpenses').text(money(total));
		$('#totalDelta').text('—');
		$('#dailyAverage').text(money(total / days));
		$('#dailyDays').text(`Across ${days} days`);

		if (cats.length) {
			$('#topCategory').text(cats[0][0]);
			$('#topCategoryMeta').text(`${money(cats[0][1], 0)} · ${Math.round((cats[0][1] / total) * 100)}% of spend`);
		} else {
			$('#topCategory').text('—');
			$('#topCategoryMeta').text('No expenses yet');
		}

		if (byDay.length) {
			const count = expenses.filter((e) => e.date === byDay[0][0]).length;
			$('#biggestDay').text(dayMonth(byDay[0][0]));
			$('#biggestDayMeta').text(`${money(byDay[0][1], 0)} in ${count} expense${count === 1 ? '' : 's'}`);
		} else {
			$('#biggestDay').text('—');
			$('#biggestDayMeta').text('No expenses yet');
		}
	}

	function renderDelta(prevTotal) {
		const total = expenses.reduce((s, e) => s + e.amount, 0);
		const prevName = MONTHS[(state.month + 10) % 12];
		if (!prevTotal) { $('#totalDelta').text(`No data for ${prevName}`); return; }
		const pct = Math.round(((total - prevTotal) / prevTotal) * 100);
		const cls = pct > 0 ? 'mr-up' : pct < 0 ? 'mr-down' : '';
		const arrow = pct > 0 ? '▲' : pct < 0 ? '▼' : '';
		$('#totalDelta').html(`<span class="${cls}">${arrow} ${Math.abs(pct)}%</span> vs ${prevName}`);
	}

	// ---------- daily chart: bar (per day) + line (running total) ----------
	function renderDailyChart() {
		const days = new Date(state.year, state.month, 0).getDate();
		const daily = Array(days).fill(0);
		expenses.forEach((e) => {
			const d = parseDate(e.date);
			if (d.getMonth() + 1 === state.month && d.getFullYear() === state.year) daily[d.getDate() - 1] += e.amount;
		});
		let run = 0;
		const cumulative = daily.map((v) => (run += v));
		const tick = (v) => '₹' + Number(v).toLocaleString('en-IN');

		if (chart) chart.destroy();
		chart = new Chart(document.getElementById('dailyExpenseChart'), {
			data: {
				labels: daily.map((_, i) => i + 1),
				datasets: [
					{
						type: 'bar', label: 'Daily total', data: daily, yAxisID: 'y',
						backgroundColor: '#818cf8', borderRadius: 4, order: 2
					},
					{
						type: 'line', label: 'Running total', data: cumulative, yAxisID: 'y1',
						borderColor: '#e8590c', backgroundColor: '#e8590c', borderWidth: 2.5,
						tension: 0.25, pointRadius: 0, pointHoverRadius: 4, order: 1
					}
				]
			},
			options: {
				responsive: true, maintainAspectRatio: false,
				interaction: { mode: 'index', intersect: false },
				plugins: {
					legend: { position: 'bottom', labels: { usePointStyle: true, boxWidth: 8 } },
					tooltip: {
						callbacks: {
							title: (items) => `${items[0].label} ${MONTHS[state.month - 1]}`,
							label: (c) => ` ${c.dataset.label}: ${money(c.parsed.y)}`
						}
					}
				},
				scales: {
					x: { grid: { display: false }, ticks: { maxTicksLimit: 16 } },
					y: { beginAtZero: true, ticks: { callback: tick }, grid: { color: '#eceefa' } },
					y1: { beginAtZero: true, position: 'right', ticks: { callback: tick }, grid: { drawOnChartArea: false } }
				}
			}
		});
	}

	// ---------- category bars (top 5) ----------
	function renderCategories() {
		const cats = groupSum(expenses, (e) => e.category).slice(0, 5);
		if (!cats.length) { $('#categoryList').html('<p class="text-secondary mb-0">No expenses this month.</p>'); return; }
		const max = cats[0][1];
		$('#categoryList').html(cats.map(([name, amt]) => `
      <div class="mr-row">
        <span title="${esc(name)}">${esc(name)}</span>
        <div class="mr-bar"><div style="width:${(amt / max) * 100}%"></div></div>
        <b>${money(amt, 0)}</b>
      </div>`).join(''));
	}

	// ---------- payment method stacked bar ----------
	function renderPayments() {
		const pays = groupSum(expenses, (e) => e.paymentMethod);
		const total = pays.reduce((s, p) => s + p[1], 0);
		if (!pays.length) {
			$('#paymentStack').empty().attr('aria-label', 'No payment data');
			$('#paymentList').html('<p class="text-secondary mb-0">No payment data this month.</p>');
			return;
		}
		const pct = (v) => Math.round((v / total) * 100);
		$('#paymentStack')
			.attr('aria-label', pays.map((p) => `${p[0]} ${pct(p[1])} percent`).join(', '))
			.html(pays.map((p, i) =>
				`<div style="width:${(p[1] / total) * 100}%;background:${PAY_COLORS[i % PAY_COLORS.length]}"></div>`).join(''));
		$('#paymentList').html(pays.map((p, i) => `
      <div class="mr-pm">
        <span><i class="mr-dot" style="background:${PAY_COLORS[i % PAY_COLORS.length]}"></i>${esc(p[0])}</span>
        <b>${money(p[1], 0)} · ${pct(p[1])}%</b>
      </div>`).join(''));
	}

	// ---------- table + search ----------
	function renderTable() {
		const q = ($('#expenseSearch').val() || '').trim().toLowerCase();
		const rows = expenses
			.filter((e) => !q || [e.title, e.category, e.paymentMethod].some((v) => String(v).toLowerCase().includes(q)))
			.sort((a, b) => b.amount - a.amount);

		$('#monthlyExpenseTableBody').html(rows.length
			? rows.map((e) => `
        <tr>
          <td>${esc(e.title)}</td>
          <td>${dayMonth(e.date)}</td>
          <td><span class="mr-tag">${esc(e.category)}</span></td>
          <td>${esc(e.paymentMethod)}</td>
          <td>${money(e.amount)}</td>
        </tr>`).join('')
			: '<tr><td colspan="5" class="mr-empty">No expenses found.</td></tr>');

		$('#expenseResultCount').text(`Showing ${rows.length} of ${expenses.length} expense${expenses.length === 1 ? '' : 's'}`);
		$('#expenseTotalAmount').text('Total ' + money(rows.reduce((s, e) => s + e.amount, 0)));
	}

	// ---------- events ----------
	$(function() {
		$('#prevMonth').on('click', () => {
			state.month === 1 ? (state.month = 12, state.year--) : state.month--;
			load();
		});
		$('#nextMonth').on('click', () => {
			state.month === 12 ? (state.month = 1, state.year++) : state.month++;
			load();
		});
		$('#expenseSearch').on('input', renderTable);
		load();
	});
})(jQuery);
