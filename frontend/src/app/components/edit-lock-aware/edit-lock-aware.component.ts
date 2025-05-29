import {
  DestroyRef,
  Directive,
  inject,
  OnInit,
  OnDestroy,
} from "@angular/core";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { EditLockService } from "../../core/services/edit-lock.service";

@Directive()
export abstract class EditLockAwareComponent implements OnInit, OnDestroy {
  protected readonly destroyRef = inject(DestroyRef);
  protected readonly editLock = inject(EditLockService);

  editingBy: String | null = null;

  abstract entity: string;
  abstract entityId: number;
  abstract currentUsername: string;

  get isLockedByOther(): boolean {
    return !!this.editingBy && this.editingBy !== this.currentUsername;
  }

  ngOnInit(): void {
    this.editLock
      .getCurrentEditor(this.entity, this.entityId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (username) => {
          this.editingBy = username;
          if (!username || username === this.currentUsername) {
            this.editLock
              .startEditing(this.entity, this.entityId)
              .pipe(takeUntilDestroyed(this.destroyRef))
              .subscribe((lockResult) => {
                if (lockResult) {
                  // Received username of current editor (409)
                  this.editingBy = lockResult.editingBy;
                } else {
                  // Lock acquired
                  this.editingBy = this.currentUsername;
                }
              });
          }
        },
        error: () => {
          this.editingBy = null;
        },
      });
  }

  ngOnDestroy(): void {
    this.editLock.stopEditing(this.entity, this.entityId).subscribe();
  }
}
