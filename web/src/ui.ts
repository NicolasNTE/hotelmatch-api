import { Component, Injectable, effect, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatPaginatorIntl } from '@angular/material/paginator';
import { firstValueFrom } from 'rxjs';
import { lang, t } from './i18n';

/** Confirmación accesible con Angular Material; reemplaza al confirm() del navegador. */
@Component({
  selector: 'hm-confirm',
  imports: [MatDialogModule, MatButtonModule],
  template: `
    <h2 mat-dialog-title>{{t('Confirmar','Confirm')}}</h2>
    <mat-dialog-content><p>{{data.message}}</p></mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button [mat-dialog-close]="false">{{t('Volver','Go back')}}</button>
      <button mat-flat-button [mat-dialog-close]="true">{{t('Sí, continuar','Yes, continue')}}</button>
    </mat-dialog-actions>`,
})
export class ConfirmDialog {
  t = t;
  data = inject<{ message: string }>(MAT_DIALOG_DATA);
}

export async function ask(dialog: MatDialog, message: string): Promise<boolean> {
  const ref = dialog.open(ConfirmDialog, { data: { message }, width: '420px', maxWidth: '94vw' });
  return (await firstValueFrom(ref.afterClosed())) === true;
}

/** Textos del paginador en el idioma activo. */
@Injectable()
export class PaginatorText extends MatPaginatorIntl {
  constructor() {
    super();
    effect(() => {
      lang();
      this.previousPageLabel = t('Página anterior', 'Previous page');
      this.nextPageLabel = t('Página siguiente', 'Next page');
      this.itemsPerPageLabel = t('Elementos por página', 'Items per page');
      this.changes.next();
    });
  }

  override getRangeLabel = (page: number, pageSize: number, length: number): string => {
    if (length === 0) {
      return t('0 de 0', '0 of 0');
    }
    const start = page * pageSize + 1;
    return `${start}–${Math.min(start + pageSize - 1, length)} ${t('de', 'of')} ${length}`;
  };
}
