// ─────────────────────────────────────────────
// edit-lock-dialog-base.ts  (generic + safe id)
// ─────────────────────────────────────────────
import {
  AfterViewInit,
  Directive,
  Inject,
  OnDestroy,
  OnInit,
} from "@angular/core";
import { FormGroup } from "@angular/forms";
import { MatDialogRef, MAT_DIALOG_DATA } from "@angular/material/dialog";
import { interval, of, Subscription } from "rxjs";
import { catchError, switchMap } from "rxjs/operators";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";

import { EditLockAwareComponent } from "src/app/components/edit-lock-aware/edit-lock-aware.component";
import {
  EditLockService,
  EditLockResult,
} from "src/app/core/services/edit-lock.service";
import { RefreshService } from "src/app/core/services/refresh.service";

/** helper */
export type WithId<T> = T & { id: number };

@Directive()
export abstract class EditLockDialogBase<T extends { id?: number }>
  extends EditLockAwareComponent
  implements OnInit, AfterViewInit, OnDestroy
{
  /** ↓ every concrete dialog must set this */
  abstract entity: string;
  /** ↓ your form in the concrete dialog */
  abstract form: FormGroup;

  abstract entityId: number;
  currentUsername: string;
  editingBy: string | null = null;
  waitingForLock = false;

  private dialogClosed = false;
  private heartbeatSub?: Subscription;

  protected constructor(
    @Inject(MAT_DIALOG_DATA) public data: T,
    public dialogRef: MatDialogRef<any>,
    protected editLock: EditLockService,
    protected refresh: RefreshService
  ) {
    super();
    this.dialogRef.afterClosed().subscribe(() => (this.dialogClosed = true));
  }

  // must be provided by the concrete dialog
  protected abstract onLockAcquired(): void;
  protected abstract onLockLost(): void;
  protected abstract hasChanged(): boolean; // every form has different fields, therefore we need to check only shown fields
  /* ───────── helpers for templates ───────── */
  get isLockedByOther(): boolean {
    return !!this.editingBy && this.editingBy !== this.currentUsername;
  }

  isEqual(a: any, b: any): boolean {
    const aKeys = Object.keys(a);
    const bKeys = Object.keys(b);
    if (aKeys.length !== bKeys.length) return false;
    for (const key of aKeys) {
      if (a[key] !== b[key]) return false;
    }
    return true;
  }

  /* ───────── PUBLIC API FOR CHILD CLASS ───────── */
  protected acquireLock(): void {
    this.waitingForLock = true;
    this.editLock
      .startEditing(this.entity, this.entityId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((res: EditLockResult) => {
        this.waitingForLock = false;
        if (res.acquired) {
          this.editingBy = null;
          this.onLockAcquired();
          this.startHeartbeat();
        } else {
          this.editingBy = res.editingBy ?? "UNKNOWN";
          this.onLockLost();
        }
      });
  }

  /* ───────── PRIVATE ───────── */
  private startHeartbeat() {
    this.heartbeatSub?.unsubscribe();
    this.heartbeatSub = interval(10_000)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        switchMap(() =>
          this.editLock
            .touch(this.entity, this.entityId)
            .pipe(catchError(() => of(void 0)))
        )
      )
      .subscribe();
  }

  ngAfterViewInit(): void {
    this.refresh.editLock$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((evt) => {
        if (evt.entityId !== this.entityId) return;

        if (evt.action === "LOCK") {
          this.editingBy = evt.username;
          this.onLockLost();
        }

        if (evt.action === "UNLOCK") {
          this.editingBy = null;
          if (!this.dialogClosed) this.acquireLock();
        }
      });
  }

  ngOnDestroy(): void {
    super.ngOnDestroy?.();
    this.heartbeatSub?.unsubscribe();
    this.editLock.stopEditing(this.entity, this.entityId).subscribe();
  }
}
