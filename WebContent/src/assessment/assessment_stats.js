import Chart from 'chart.js/auto';
class AssessmentStats {
	constructor(){
		this.assessmentId = document.getElementById("assessmentId").value;
		this.vulnBarChart;
		this.severityNames=[];
		this.bgColors=[];
		this.borderColors=[];
		this.allColors=[];
		this.allBorderColors=[];
	}
	
	createBarChart(vulns){
		const ctx = document.getElementById('vulnStats');
		let dataset = {"label" : "findings"};
		dataset.data = vulns.reduce( (acc, vuln) => {acc.push(vuln.count);return acc;},[]);
		dataset.backgroundColor = this.bgColors;
		dataset.borderColor = this.borderColors;
		dataset.borderWidth = 2;
		let labels =[];
		this.severityNames.forEach( (name, index) => labels.push(`${name}  ${dataset.data[index]}`))
		this.vulnBarChart = new Chart(ctx, {
			type: 'bar',
			data: {
				labels: labels,
				datasets: [dataset]
			},
			options: {
				maintainAspectRatio: false,
				plugins: {
					legend: {
						display: false
					}
				},
				indexAxis: 'y',
				scales: {
					y: {
						beginAtZero: true
					},
					x: {
						display: false
					}
				}
			}
		});
	}
	createPieChart(categories){
		if(categories.length == 0){
			categories = [{"category" : "No Issues", "count": 1}]
		}
		const ctx = document.getElementById('catStats');
		let dataset = {"label" : "categories"};
		let categoryNames = [];
		dataset.data = categories.reduce( (acc, cat) => {acc.push(cat.count);categoryNames.push(cat.category);return acc;},[]);
		dataset.backgroundColor = this.allColors.reverse();
		dataset.borderColor = this.allBorderColors.reverse();
		dataset.borderWidth = 2;
		
		this.vulnPieChart = new Chart(ctx, {
			type: 'doughnut',
			data: {
				labels: categoryNames,
				datasets: [dataset]
			},
			options: {
				plugins: {
					legend: {
						display: false
					}
				}
			}
		});
		
	}
	
	requestStats(callback){
		$.get(`GetStats?id=${this.assessmentId}`).done(callback)
	}

	createCharts() {
		this.requestStats((resp) => {
			try{
			const vulns = resp.data.vulns;
			const categories = resp.data.categories;
			this.severityNames= resp.data.severityNames;
			this.allColors = [];
			this.allBorderColors = resp.data.colors;
			const l = this.severityNames.length;
			this.severityNames.forEach( (_label, index) =>  {
				this.bgColors.push(resp.data.colors[index +l -2]+"22");
				this.borderColors.push(resp.data.colors[index +l -2 ]);
				}
			);
			resp.data.colors.forEach( (color, _index) => this.allColors.push(color+"22"));
			this.createBarChart(vulns)
			this.createPieChart(categories)
			}catch(e){
				console.log(e)
			}
			
		})
		
	}
	
	updateAllStats(){
		this.requestStats( (resp) => {
			try{
			const vulns = resp.data.vulns;
			let categories = resp.data.categories;
			if(categories.length == 0){
				categories = [{"category" : "No Issues", "count": 1}]
			}
			let categoryNames = [];
			let categoryDataset = categories.reduce( (acc, cat) => {acc.push(cat.count);categoryNames.push(cat.category);return acc;},[]);
			this.vulnPieChart.data.labels = categoryNames;
			this.vulnPieChart.data.datasets[0].data = categoryDataset;
			this.vulnPieChart.update();
			
			let labels =[];
			let vulnDataset  = vulns.reduce( (acc, vuln) => {acc.push(vuln.count);return acc;},[]);
			this.severityNames.forEach( (name, index) => labels.push(`${name}  ${vulnDataset[index]}`))
			this.vulnBarChart.data.labels = labels;
			this.vulnBarChart.data.datasets[0].data = vulnDataset;

			this.vulnBarChart.update();
			}catch(e){
				console.log(e)
			}
			
		});
	}
	
	updateVulnCount(index, count){
		let label = this.vulnBarChart.data.labels[index];
		label = label.split(" ")[0];
		label = `${label} ${count}`
		this.vulnBarChart.data.labels[index] = label;
		this.vulnBarChart.data.datasets[0].data[index]= count;
		this.vulnBarChart.update();
	}
	updateCatCount(catNames, catDataset){
		this.createPieChart.data.datasets = catCounts;
		this.createPieChart.data.labels = catNames;
		this.vulnPieChart.update();
	}
}
let assessmentStats = new AssessmentStats();
assessmentStats.createCharts();
window.addEventListener(
  "message",
  (event) => {
	if(event.data.type == "updateVuln"){
		  let sevId = event.data.sevId;
		  let count = event.data.count;
		  assessmentStats.updateVulnCount(sevId,count);
		  setTimeout(()=>{assessmentStats.updateAllStats()},2000);
	}else if(event.data.type == "updateStats"){
		assessmentStats.updateAllStats();
	}

  },
  false
);