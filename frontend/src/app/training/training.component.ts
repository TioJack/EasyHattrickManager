import {AfterViewInit, Component, ElementRef, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {TrainingInfo} from '../services/model/data-response';
import {PlayService} from '../services/play.service';
import {TrainingStats} from '../services/model/training-stats';
import {NgIf} from '@angular/common';
import {Chart, ChartConfiguration, registerables} from 'chart.js';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';
import {FirstCapitalizePipe} from '../pipes/first-capitalize.pipe';
import {trainingTypeColor} from '../constants/global.constant';

Chart.register(...registerables);

@Component({
  selector: 'app-training',
  imports: [
    NgIf,
    TranslatePipe,
    FirstCapitalizePipe
  ],
  templateUrl: './training.component.html'
})
export class TrainingComponent implements OnInit, AfterViewInit, OnDestroy {
  training: TrainingInfo | null = null;
  trainingStats: TrainingStats | null = null;

  @ViewChild('trainingChart') trainingChartRef?: ElementRef<HTMLCanvasElement>;
  private chart?: Chart<"bar", Array<number | [number, number] | null>, unknown>;

  constructor(
    private playService: PlayService,
    private translateService: TranslateService
  ) {
  }

  ngOnInit(): void {
    this.playService.training$.subscribe(training => {
      this.training = training;
    });
    this.playService.trainingStats$.subscribe(trainingStats => {
      if (trainingStats) {
        this.trainingStats = trainingStats;
        this.updateChart();
      } else {
        this.trainingStats = null;
        this.destroyChart();
      }
    });
    this.translateService.onLangChange.subscribe(() => {
      this.updateChart();
    });
  }

  ngAfterViewInit(): void {
    this.updateChart();
  }

  ngOnDestroy(): void {
    this.destroyChart();
  }

  public trainingTypeColor = trainingTypeColor;

  private updateChart(): void {
    const canvas = this.trainingChartRef?.nativeElement;
    if (!canvas || !this.trainingStats) {
      return;
    }
    const entries = Object.entries(this.trainingStats.trainings ?? {});
    const data = entries
      .map(([k, v]) => ({
        typeId: Number(k),
        label: this.translateService.instant('ht.training-type-' + k),
        value: Number(v ?? 0)
      }))
      .sort((a, b) => b.value - a.value);
    const labels = data.map(d => this.truncateLabel(d.label));
    const values = data.map(d => d.value);
    const valueLabelsPlugin = {
      id: 'valueLabels',
      afterDatasetsDraw: (chart: Chart) => {
        const {ctx} = chart;
        const meta = chart.getDatasetMeta(0);
        const dataset = chart.data.datasets[0] as any;
        const valuesLocal: number[] = (dataset?.data as number[]) ?? values;
        ctx.save();
        ctx.font = '600 12px system-ui, -apple-system, Segoe UI, Roboto, Helvetica, Arial, sans-serif';
        ctx.textBaseline = 'middle';
        ctx.fillStyle = '#ffffff';
        ctx.textAlign = 'right';
        const padding = 4;
        meta.data.forEach((element: any, i: number) => {
          const val = valuesLocal[i];
          if (val == null) return;
          const barStart = Math.min(element.x, element.base);
          const barEnd = Math.max(element.x, element.base);
          const y = element.y;
          const text = String(val);
          const textWidth = ctx.measureText(text).width;
          let x = barEnd - padding;
          const minXForText = barStart + padding + textWidth;
          if (x < minXForText) {
            x = minXForText;
          }
          ctx.fillText(text, x, y);
        });
        ctx.restore();
      }
    };
    const barColors = data.map(d => trainingTypeColor(d.typeId));
    const config: ChartConfiguration<'bar'> = {
      type: 'bar',
      data: {
        labels,
        datasets: [
          {
            data: values,
            backgroundColor: barColors,
            borderRadius: 2,
            borderSkipped: false,
            maxBarThickness: 36,
            categoryPercentage: 0.8,
            barPercentage: 0.9
          }
        ]
      },
      options: {
        indexAxis: 'y',
        responsive: true,
        maintainAspectRatio: false,
        animation: false,
        events: [],
        scales: {
          x: {
            beginAtZero: true,
            ticks: {display: false, precision: 0, stepSize: 1},
            grid: {display: false, drawTicks: false},
            border: {display: false}
          },
          y: {
            ticks: {autoSkip: false},
            grid: {display: false, drawTicks: false},
          }
        },
        plugins: {
          legend: {display: false},
          tooltip: {
            enabled: false
          }
        }
      },
      plugins: [valueLabelsPlugin]
    };
    this.destroyChart();
    this.chart = new Chart(canvas.getContext('2d')!, config);
  }

  private truncateLabel(text: string, max = 22): string {
    if (!text) return '';
    return text.length > max ? text.slice(0, max - 1).trimEnd() + '…' : text;
  }

  private destroyChart(): void {
    if (this.chart) {
      this.chart.destroy();
      this.chart = undefined;
    }
  }
}
