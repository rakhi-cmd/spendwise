/**
 * @author Rakhi Sharma
 */

document.addEventListener("DOMContentLoaded", function() {
	const data = window.spendWiseDashboardData || {};
	const colors = ["#347cf0", "#20b879", "#ff981c", "#8954df", "#ec4c91", "#37bfe0", "#f2c94c"];
	const values = list => (list || []).map(value => Number(value) || 0);
	const money = value => new Intl.NumberFormat("en-IN", { 
		style: "currency", 
		currency: "INR", 
		maximumFractionDigits: 2 
	}).format(Number(value) || 0);

	function emptyChart(id, labels) {
		const canvas = document.getElementById(id);
		if (canvas && (!labels || !labels.length)) {
			const note = document.createElement("div");
			note.className = "dashboard-empty";
			note.textContent = "No active expense data available for this period.";
			canvas.parentElement.appendChild(note);
			canvas.style.display = "none";
		}
	}
	function legend(id, labels, amounts) {
		const container = document.getElementById(id);
		if (!container) return;
		container.replaceChildren();
		if (!labels || !labels.length) {
			const note = document.createElement("div"); 
			note.className = "dashboard-empty"; 
			note.textContent = "No data available"; 
			container.appendChild(note); 
			return;
		}
		labels.forEach((label, i) => {
			const row = document.createElement("div"); 
			row.className = "legend-row";
			const dot = document.createElement("span"); 
			dot.className = "legend-dot"; 
			dot.style.backgroundColor = colors[i % colors.length];
			const name = document.createElement("span"); 
			name.className = "legend-name"; 
			name.textContent = label; 
			name.title = label;
			const amount = document.createElement("span"); 
			amount.className = "legend-value"; 
			amount.textContent = money(amounts[i]);
			row.append(dot, name, amount); 
			container.appendChild(row);
		});
	}
	if (typeof Chart === "undefined") { 
		console.error("Chart.js did not load."); 
		return; 
	}
	Chart.defaults.font.family = "Arial, sans-serif"; 
	Chart.defaults.color = "#344563";

	const monthLabels = data.monthlyLabels || [], monthValues = values(data.monthlyValues);
	emptyChart("monthlyExpensesChart", monthLabels);
	if (monthLabels.length) new Chart(document.getElementById("monthlyExpensesChart"),
			{
				type: "bar", 
				data: { 
					labels: monthLabels, 
					datasets: [{ 
						data: monthValues, 
						backgroundColor: "#347cf0", 
						borderRadius: 4, 
						maxBarThickness: 48 }] 
					},
				options: { 
					responsive: true, 
					maintainAspectRatio: false, 
					plugins: { 
						legend: { 
							display: false 
						}, 
						tooltip: { 
							callbacks: { 
								label: c => money(c.raw) 
							} 
						} 
					}, 
					scales: { 
						x: { 
							grid: { 
								display: false 
							} 
						}, 
						y: { 
							beginAtZero: true, 
							ticks: { 
								callback: v => money(v) 
							}, 
							grid: { 
								color: "#edf1f7" 
							} 
						} 
					} 
				}
			});

	const catLabels = data.categoryLabels || [], catValues = values(data.categoryValues);
	emptyChart("categoryExpensesChart", catLabels); 
	legend("categoryChartLegend", catLabels, catValues);
	if (catLabels.length) new Chart(document.getElementById("categoryExpensesChart"), 
	{
		type: "doughnut", 
		data: { 
			labels: catLabels, 
			datasets: [{ 
				data: catValues, 
				backgroundColor: colors, 
				borderWidth: 0, 
				hoverOffset: 5 }] 
			},
		options: { 
			responsive: true, 
			maintainAspectRatio: false, 
			cutout: "62%", 
			plugins: { 
				legend: { 
					display: false 
				}, 
				tooltip: { 
					callbacks: { 
						label: c => c.label + ": " + money(c.raw) 
					} 
				} 
			} 
		}
	});

	const subLabels = data.subcategoryLabels || [], subValues = values(data.subcategoryValues);
	emptyChart("subcategoryExpensesChart", subLabels);
	if (subLabels.length) new Chart(document.getElementById("subcategoryExpensesChart"), 
	{
		type: "bar", 
		data: { 
			labels: subLabels, 
			datasets: [{ 
				data: subValues, 
				backgroundColor: subLabels.map((_, i) => colors[i % colors.length]), 
				borderRadius: 4, 
				barThickness: 18 }] 
			},
		options: { 
			indexAxis: "y", 
			responsive: true, 
			maintainAspectRatio: false, 
			plugins: { 
				legend: { 
					display: false 
				}, 
				tooltip: { 
					callbacks: { 
						label: c => money(c.raw) 
					} 
				} 
			}, 
			scales: { 
				x: { 
					beginAtZero: true, 
					ticks: { 
						callback: v => money(v) 
					}, 
					grid: { 
						color: "#edf1f7" 
					} 
				}, 
				y: { 
					grid: { 
						display: false 
					} 
				} 
			} 
		}
	});

	const payLabels = data.paymentLabels || [], payValues = values(data.paymentValues);
	emptyChart("paymentMethodsChart", payLabels); legend("paymentChartLegend", payLabels, payValues);
	if (payLabels.length) new Chart(document.getElementById("paymentMethodsChart"), {
		type: "doughnut", 
		data: { 
			labels: payLabels, 
			datasets: [{ 
				data: payValues, 
				backgroundColor: colors, 
				borderWidth: 0, 
				hoverOffset: 5 
			}] 
		},
		options: { 
			responsive: true, 
			maintainAspectRatio: false, 
			cutout: "62%", 
			plugins: { 
				legend: { 
					display: false 
				}, 
				tooltip: { 
					callbacks: { 
						label: c => c.label + ": " + money(c.raw) 
					} 
				} 
			} 
		}
	});
});
