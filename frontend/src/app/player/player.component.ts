import {Component, ElementRef, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {PlayerInfo, PlayerTrainingInfo} from '../services/model/data-response';
import {Chart, ChartConfiguration, ChartEvent, LegendElement, LegendItem, registerables} from 'chart.js';
import {ActivatedRoute} from '@angular/router';
import {DataService} from '../services/data.service';
import {WeekInfo} from './model/week-info';
import {TranslateService} from '@ngx-translate/core';
import {DatePipe, NgIf} from '@angular/common';
import {DEFAULT_DATE_FORMAT} from '../constants/global.constant';
import {PlayerDataResponse} from '../services/model/player-data-response';

Chart.register(...registerables);

@Component({
  selector: 'app-player',
  standalone: true,
  imports: [
    NgIf
  ],
  providers: [DatePipe],
  templateUrl: './player.component.html',
  styleUrls: ['./player.component.scss']
})
export class PlayerComponent implements OnInit, OnDestroy {
  @ViewChild('playerStatsChart') skillsChartRef?: ElementRef<HTMLCanvasElement>;
  chart?: Chart<'line'>;
  playerId: number | undefined;
  playerDataResponse: PlayerDataResponse | null = null;
  player: PlayerInfo | undefined;
  dateFormat: string = DEFAULT_DATE_FORMAT;
  translations: { [key: string]: string } = {};
  translationsKeys = [
    'ehm.season',
    'ehm.week',
    'ehm.years',
    'ehm.days',
    'ehm.tsi',
    'ehm.wage',
    'ehm.training',
    'ehm.skill-level',
    'ehm.main-skills-group',
    'ehm.status-group',
    'ehm.trainer-skills-group',
    'ehm.global-skills-group',
    'ht.skill-keeper',
    'ht.skill-defender',
    'ht.skill-playmaker',
    'ht.skill-winger',
    'ht.skill-passer',
    'ht.skill-scorer',
    'ht.skill-kicker',
    'ht.skill-form',
    'ht.skill-stamina',
    'ht.skill-experience',
    'ht.skill-leadership'
  ];

  constructor(
    private route: ActivatedRoute,
    private dataService: DataService,
    private translateService: TranslateService,
    private datePipe: DatePipe) {
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.playerId = +params['playerId'];
      this.dataService.getPlayerData(this.playerId).subscribe(playerDataResponse => {
        this.playerDataResponse = playerDataResponse;
        if (this.playerDataResponse) {
          this.playerDataResponse.weeklyData.forEach(weeklyInfo => {
            let player = weeklyInfo.player;
            player.salary = this.applyCurrencyRate(player.salary, this.playerDataResponse!.userConfig.currency.currencyRate);
            if (player.playerTraining) {
              player.playerTraining.keeper = this.getMaxTrainingValue(player.playerTraining);
            } else {
              player.playerTraining = {defender: 0, passing: 0, playmaker: 0, scorer: 0, setPieces: 0, winger: 0, keeper: 0};
            }
          });
        }
        this.dateFormat = this.playerDataResponse?.userConfig.dateFormat ?? DEFAULT_DATE_FORMAT;
        this.player = this.playerDataResponse?.weeklyData?.flatMap(weeklyInfo => weeklyInfo.player).reverse()[0];
        this.translateService.use(this.playerDataResponse?.userConfig.languageId?.toString() ?? '2');
        this.translateService.get(this.translationsKeys).subscribe((translations) => {
          this.translations = translations;
          this.renderChart();
        });
      });
    });
  }

  ngOnDestroy(): void {
    this.destroyChart();
  }

  private applyCurrencyRate(value: number, rate: number): number {
    return value > 0 ? Math.round(value / rate) : -1;
  }

  private getMaxTrainingValue(trainingInfo: PlayerTrainingInfo): number {
    const values = Object.values(trainingInfo);
    return Math.max(...values);
  }

  private renderChart(): void {
    if (!this.skillsChartRef || !this.playerId) {
      return;
    }
    const canvas = this.skillsChartRef.nativeElement;
    const stats = this.getPlayerStats();
    const dataSetGroups = this.getDataSetGroups(stats);

    const config: ChartConfiguration<'line'> = {
      type: 'line',
      data: {
        labels: stats.map(stat => `${stat.player.age}${this.translations['ehm.years'].charAt(0)} ${stat.player.ageDays}${this.translations['ehm.days'].charAt(0)}`),
        datasets: dataSetGroups.flatMap((group) => group.datasets),
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        animation: false,
        scales: this.getScales(),
        plugins: {
          legend: {
            display: true,
            position: 'right',
            labels: {
              boxWidth: 20,
              boxHeight: 20,
              font: {
                size: 12,
              },
              padding: 10,
              usePointStyle: false,
              generateLabels: (chart) => this.generateGroupLabels(dataSetGroups, chart),
            },
            onClick: (e, legendItem, chart) => this.onLegendClick(e, legendItem, chart, dataSetGroups)
          },
          tooltip: {
            enabled: true,
            intersect: false,
            mode: 'index',
            position: 'nearest',
            callbacks: {title: this.getTooltipTitle(stats)}
          }
        }
      },
      plugins: [this.verticalLinePlugin()]
    };
    this.destroyChart();
    this.chart = new Chart(canvas.getContext('2d')!, config);
  }

  private destroyChart(): void {
    if (this.chart) {
      this.chart.destroy();
      this.chart = undefined;
    }
  }

  private getPlayerStats(): { week: WeekInfo, player: PlayerInfo }[] {
    if (!this.playerDataResponse || !this.playerId) {
      return [];
    }
    const stats: { week: WeekInfo, player: PlayerInfo }[] = [];
    this.playerDataResponse.weeklyData.forEach((weeklyInfo) => {
      stats.push({week: {season: weeklyInfo.season, week: weeklyInfo.week, date: weeklyInfo.date}, player: weeklyInfo.player});
    });
    return stats;
  }

  private getScales(): any {
    return {
      x: {
        grid: {display: false}
      },
      ySkill: {
        beginAtZero: true,
        min: 0,
        suggestedMax: 20,
        ticks: {stepSize: 1},
        grid: {display: true},
        display: true,
        title: {
          display: true,
          text: this.translations['ehm.skill-level'],
          align: 'center',
          padding: {top: 5, bottom: 0}
        }
      },
      yTSI: {
        beginAtZero: true,
        min: 0,
        ticks: {stepSize: 500},
        grid: {display: false},
        display: false,
        title: {
          display: true,
          text: this.translations['ehm.tsi'],
          align: 'center',
          padding: {top: 5, bottom: 0}
        }
      },
      ySalary: {
        beginAtZero: true,
        min: 0,
        ticks: {stepSize: 500},
        grid: {display: false},
        display: false,
        title: {
          display: true,
          text: this.translations['ehm.wage'],
          align: 'center',
          padding: {top: 5, bottom: 0}
        }
      },
      yHTMS: {
        beginAtZero: true,
        min: 0,
        suggestedMax: 2500,
        ticks: {stepSize: 5},
        grid: {display: false},
        display: false,
        title: {
          display: true,
          text: 'HTMS',
          align: 'center',
          padding: {top: 5, bottom: 0}
        }
      },
      yTraining: {
        beginAtZero: true,
        min: 0,
        suggestedMax: 100,
        ticks: {stepSize: 5},
        grid: {display: false},
        display: false,
        title: {
          display: true,
          text: this.translations['ehm.training'],
          align: 'center',
          padding: {top: 5, bottom: 0}
        }
      }
    };
  }

  private getDataSetGroups(stats: { week: WeekInfo; player: PlayerInfo }[]) {
    return [
      {
        name: this.translations['ehm.main-skills-group'],
        datasets: [
          this.setDataSet(this.translations['ht.skill-keeper'], stats.map(stat => stat.player.keeperSkill), '#2F6C8E', 'ySkill', true),
          this.setDataSet(this.translations['ht.skill-defender'], stats.map(stat => stat.player.defenderSkill), '#7E57C2', 'ySkill', true),
          this.setDataSet(this.translations['ht.skill-playmaker'], stats.map(stat => stat.player.playmakerSkill), '#2E7D32', 'ySkill', true),
          this.setDataSet(this.translations['ht.skill-winger'], stats.map(stat => stat.player.wingerSkill), '#26A69A', 'ySkill', true),
          this.setDataSet(this.translations['ht.skill-passer'], stats.map(stat => stat.player.passingSkill), '#F9A825', 'ySkill', true),
          this.setDataSet(this.translations['ht.skill-scorer'], stats.map(stat => stat.player.scorerSkill), '#D84315', 'ySkill', true),
          this.setDataSet(this.translations['ht.skill-kicker'], stats.map(stat => stat.player.setPiecesSkill), '#546E7A', 'ySkill', true),
        ]
      },
      {
        name: this.translations['ehm.status-group'],
        datasets: [
          this.setDataSet(this.translations['ht.skill-form'], stats.map(stat => stat.player.playerForm), '#4CAF50', 'ySkill', false),
          this.setDataSet(this.translations['ht.skill-stamina'], stats.map(stat => stat.player.staminaSkill), '#2196F3', 'ySkill', false),
        ]
      },
      {
        name: this.translations['ehm.trainer-skills-group'],
        datasets: [
          this.setDataSet(this.translations['ht.skill-experience'], stats.map(stat => stat.player.experience), '#00B8D4', 'ySkill', false),
          this.setDataSet(this.translations['ht.skill-leadership'], stats.map(stat => stat.player.leadership), '#5E35B1', 'ySkill', false),
        ]
      },
      {
        name: this.translations['ehm.global-skills-group'],
        datasets: [
          this.setDataSet(this.translations['ehm.tsi'], stats.map(stat => stat.player.tsi), '#00897B', 'yTSI', false),
          this.setDataSet(this.translations['ehm.wage'], stats.map(stat => stat.player.salary), '#F4511E', 'ySalary', false),
        ]
      },
      {
        name: 'htms',
        datasets: [
          this.setDataSet('htms', stats.map(stat => stat.player.htms), '#0288D1', 'yHTMS', false),
          this.setDataSet('htms28', stats.map(stat => stat.player.htms28), '#D32F2F', 'yHTMS', false),
        ]
      },
      {
        name: this.translations['ehm.training'],
        datasets: [
          this.setDataSet(this.translations['ehm.training'], stats.map(stat => stat.player.playerTraining.keeper), '#388E3C', 'yTraining', false),
        ]
      }
    ]
  }

  private setDataSet(label: string, data: number[], color: string, yAxisID: string, enabled: boolean) {
    return {
      label: label,
      data: data.map(val => (val >= 0 ? val : null)),
      fill: false,
      borderColor: color,
      backgroundColor: color,
      pointBackgroundColor: color,
      pointRadius: 3,
      yAxisID: yAxisID,
      hidden: !enabled
    }
  }

  private generateGroupLabels(dataSetGroups: { name: string; datasets: any[] }[], chart: any): LegendItem[] {
    const labels: LegendItem[] = [];
    dataSetGroups.forEach((group, groupIndex) => {
      labels.push({
        text: group.name.toUpperCase(),
        fillStyle: '#FFFFFF',
        lineWidth: 0,
        hidden: group.datasets.every((dataset) => dataset.hidden),
        datasetIndex: -groupIndex - 1,
      });
      group.datasets.forEach((dataset, datasetIndex) => {
        const datasetIndexInChart = chart.data.datasets.indexOf(dataset);
        labels.push({
          text: dataset.label,
          fillStyle: dataset.borderColor,
          hidden: dataset.hidden,
          datasetIndex: datasetIndexInChart,
        });
      });
    });
    return labels;
  }

  private onLegendClick(e: ChartEvent, legendItem: LegendItem, legend: LegendElement<'line'>, dataSetGroups: { name: string; datasets: any[] }[]): void {
    const datasetIndex = legendItem.datasetIndex;
    if (datasetIndex === undefined) return;

    if (datasetIndex < 0) {
      const groupIndex = -datasetIndex - 1;
      const group = dataSetGroups[groupIndex];
      const allHidden = group.datasets.every((dataset) => dataset.hidden);
      group.datasets.forEach((dataset) => {
        const index = legend.chart.data.datasets.indexOf(dataset);
        if (index !== -1) {
          legend.chart.data.datasets[index].hidden = !allHidden;
        }
      });
    } else {
      const dataset = legend.chart.data.datasets[datasetIndex];
      dataset.hidden = !dataset.hidden;
    }

    const ySkillActive = legend.chart.data.datasets.some(d => d.yAxisID === 'ySkill' && !d.hidden);
    const yTSIActive = legend.chart.data.datasets.some(d => d.yAxisID === 'yTSI' && !d.hidden);
    const ySalaryActive = legend.chart.data.datasets.some(d => d.yAxisID === 'ySalary' && !d.hidden);
    const yHTMSActive = legend.chart.data.datasets.some(d => d.yAxisID === 'yHTMS' && !d.hidden);
    const yTrainingActive = legend.chart.data.datasets.some(d => d.yAxisID === 'yTraining' && !d.hidden);

    const scales = legend.chart.options.scales;
    if (scales) {
      if (scales['ySkill']) {
        scales['ySkill'].display = ySkillActive;
        if (scales['ySkill'].grid) {
          scales['ySkill'].grid.display = ySkillActive;
        }
      }
      if (scales['yTSI']) {
        scales['yTSI'].display = yTSIActive;
        if (scales['yTSI'].grid) {
          scales['yTSI'].grid.display = !ySkillActive && yTSIActive;
        }
      }
      if (scales['ySalary']) {
        scales['ySalary'].display = ySalaryActive;
        if (scales['ySalary'].grid) {
          scales['ySalary'].grid.display = !ySkillActive && !yTSIActive && ySalaryActive;
        }
      }
      if (scales['yHTMS']) {
        scales['yHTMS'].display = yHTMSActive;
        if (scales['yHTMS'].grid) {
          scales['yHTMS'].grid.display = !ySkillActive && !yTSIActive && !ySalaryActive && yHTMSActive;
        }
      }
      if (scales['yTraining']) {
        scales['yTraining'].display = yTrainingActive;
        if (scales['yTraining'].grid) {
          scales['yTraining'].grid.display = !ySkillActive && !yTSIActive && !ySalaryActive && !yHTMSActive && yTrainingActive;
        }
      }
    }
    legend.chart.update();
  }

  private getTooltipTitle(stats: { week: WeekInfo; player: PlayerInfo }[]): (tooltipItems: any) => string[] {
    return (tooltipItems) => {
      const index = tooltipItems[0].dataIndex;
      const stat = stats[index];
      return [
        `${this.translations['ehm.season']} ${stat.week.season}`,
        `${this.translations['ehm.week']} ${stat.week.week}`,
        `${this.datePipe.transform(stat.week.date, this.dateFormat)}`,
        `${stat.player.age}${this.translations['ehm.years'].charAt(0)} ${stat.player.ageDays}${this.translations['ehm.days'].charAt(0)}`
      ];
    };
  }

  private verticalLinePlugin() {
    let mouseInside = false; // Estado para verificar si el ratón está dentro del gráfico

    return {
      id: 'verticalLine',
      beforeEvent: (chart: Chart, args: any) => {
        const event = args.event;

        // Escuchar evento 'mouseleave' para saber cuándo salir del área del canvas
        if (event.type === 'mouseout') {
          mouseInside = false; // El ratón está fuera del gráfico
          chart.update(); // Forzar re-render sin línea vertical
        }

        // Escuchar evento 'mousemove' para saber si el ratón está dentro
        if (event.type === 'mousemove') {
          mouseInside = true;
        }
      },
      beforeDraw: (chart: Chart) => {
        if (mouseInside && chart.tooltip && chart.tooltip.dataPoints && chart.tooltip.dataPoints.length > 0) {
          const tooltipData = chart.tooltip.dataPoints[0];
          const x = tooltipData.element.x;
          const topY = chart.chartArea.top;
          const bottomY = chart.chartArea.bottom;
          const ctx = chart.ctx;
          ctx.save();
          ctx.beginPath();
          ctx.moveTo(x, topY);
          ctx.lineTo(x, bottomY);
          ctx.lineWidth = 1;
          ctx.strokeStyle = 'rgba(0, 0, 0, 0.7)'
          ctx.stroke();
          ctx.restore();
        }
      }
    };
  }

}
