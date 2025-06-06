import {
  AfterViewInit,
  Component,
  ElementRef,
  OnInit,
  ViewChild,
} from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { HttpClient } from "@angular/common/http";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { CommentService } from "src/app/core/services/comment-service";
import { CommentDTO } from "src/app/core/models/comment-dto.model";
import { StaffingService } from "src/app/core/services/staffing.service";
import { RefreshService } from "src/app/core/services/refresh.service";
import { KeycloakAuthService } from "src/app/core/services/keycloak-auth.service";
import { Location } from "@angular/common";

@Component({
  selector: "app-comments-view",
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <!-- 💬 Right: Comment Thread + Controls -->
    <div
      class="col-span-3 bg-white shadow-md rounded-xl p-6 max-h-[80vh] overflow-y-auto space-y-6"
    >
      <!-- 🔘 Top Buttons -->
      <div class="flex justify-between items-center mb-4">
        <!-- <h2 class="text-lg font-semibold">{{ title }}</h2> -->
        <div class="mb-2">
          <span *ngIf="!editingTitle" class="text-xl font-semibold">
            {{ title }}
            <button
              *ngIf="isAdmin() && isActive"
              (click)="editingTitle = true"
              class="ml-2 text-sm text-blue-600 hover:underline"
            >
              Edit
            </button>
          </span>

          <div *ngIf="editingTitle" class="flex items-center gap-2">
            <input
              [(ngModel)]="titleEdit"
              class="border px-2 py-1 rounded text-sm w-64"
            />
            <button
              (click)="saveTitle()"
              class="text-sm bg-blue-600 text-white px-2 py-1 rounded"
            >
              Save
            </button>
            <button
              (click)="cancelTitleEdit()"
              class="text-sm text-gray-500 hover:underline"
            >
              Cancel
            </button>
          </div>
        </div>
        <div class="flex gap-2">
          <button
            class="px-4 py-2 text-sm rounded bg-gray-200 hover:bg-gray-300 text-gray-800"
            (click)="goBack()"
          >
            ← Go Back
          </button>
          <button
            *ngIf="isActive && isAdmin()"
            class="px-4 py-2 text-sm rounded bg-green-600 hover:bg-green-700 text-white"
            (click)="markAsCompleted()"
          >
            ✅ Mark as Completed
          </button>
        </div>
      </div>
      <div
        class="col-span-2 bg-gray-100 rounded-xl shadow p-6 font-jakarta text-gray-800"
      >
        <p class="text-sm text-gray-600">
          Client:
          <span class="font-medium">
            <a
              (click)="openClient(clientId)"
              class="text-blue-600"
              style="cursor: pointer"
            >
              {{ client }}
            </a>
          </span>
        </p>
        <p class="text-sm text-gray-600">
          Employee:
          <span class="font-medium">
            <a
              (click)="openEmployeeProfile(employeeUsername)"
              class="text-blue-600"
              style="cursor: pointer"
            >
              {{ employee }}
            </a></span
          >
        </p>
      </div>
      <div class="grid grid-cols-1 md:grid-cols-1 gap-1 p-1 font-jakarta">
        <!--  Left Pane: Static Info -->

        <!--  Right Pane: Comment Thread -->
        <div
          class="col-span-3 bg-white shadow-md rounded-xl p-1 max-h-[80vh] overflow-y-auto space-y-1"
        >
          <!-- Show banner if the process is marked as completed -->

          <!-- System comment at the top -->
          <div
            *ngIf="systemComment"
            class="bg-red-50 border-l-4 border-red-600 text-red-800 p-4 mb-4"
          >
            <strong>{{ systemComment.title }}</strong>
            <p>{{ systemComment.comment }}</p>
          </div>

          <ng-container *ngFor="let comment of comments">
            <ng-container
              *ngTemplateOutlet="commentBlock; context: { $implicit: comment }"
            ></ng-container>
          </ng-container>

          <ng-template #commentBlock let-comment>
            <div class="mb-4  pb-4">
              <div
                class="flex items-center justify-between text-sm text-gray-600"
              >
                <span
                  class="font-medium"
                  *ngIf="comment.author?.username != loggedUsername"
                >
                  <a
                    (click)="openEmployeeProfile(comment.author?.username)"
                    style="cursor: pointer"
                    class="font-medium text-blue-600 dark:text-blue-500 hover:underline"
                  >
                    {{ comment.author?.firstName }}
                    {{ comment.author?.lastName }}</a
                  >
                </span>
                <span
                  class="font-medium"
                  *ngIf="comment.author?.username == loggedUsername"
                >
                  {{ comment.author?.firstName }}
                  {{ comment.author?.lastName }}
                </span>
                <span>{{ comment.createdAt | date: "medium" }}</span>
              </div>
              <p class="text-gray-800">{{ comment.comment }}</p>

              <!--  Reply Toggle Button -->
              <button
                *ngIf="visibleReplyBox !== comment.id && isActive"
                class="text-blue-500 text-sm mt-2"
                (click)="visibleReplyBox = comment.id"
              >
                Reply
              </button>

              <!--  Conditional Reply Input -->
              <div
                *ngIf="visibleReplyBox === comment.id && isActive"
                class="mt-2 ml-2"
              >
                <textarea
                  [(ngModel)]="replyInputs[comment.id]"
                  rows="2"
                  class="w-full border border-gray-300 rounded p-2 text-sm resize-none"
                  placeholder="Reply to this comment..."
                ></textarea>
                <button
                  (click)="postReply(comment)"
                  class="mt-1 bg-blue-600 text-white px-3 py-1 text-sm rounded hover:bg-blue-700"
                >
                  Reply
                </button>
              </div>

              <!-- Recursively render replies -->
              <div
                *ngIf="comment.replies?.length"
                class="ml-6 mt-4 border-l-2 pl-4 border-gray-200"
              >
                <ng-container *ngFor="let reply of comment.replies">
                  <ng-container
                    *ngTemplateOutlet="
                      commentBlock;
                      context: { $implicit: reply }
                    "
                  ></ng-container>
                </ng-container>
              </div>
            </div>
          </ng-template>

          <!--  New Root-Level Comment -->
          <div class="mb-6" *ngIf="isActive">
            <textarea
              [(ngModel)]="newComment"
              rows="3"
              class="w-full border border-gray-300 rounded-lg p-3 text-sm resize-none"
              placeholder=""
              [disabled]="!isActive"
              [placeholder]="
                isActive
                  ? 'Write a new comment...'
                  : 'Project is completed, comments are disabled'
              "
            ></textarea>
            <button
              (click)="addComment()"
              class="mt-2 bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 text-sm"
            >
              Post Comment
            </button>
          </div>
        </div>
      </div>
    </div>
  `,
})
export class CommentsViewComponent implements OnInit, AfterViewInit {
  // [x: string]: any;
  staffingId: number = 0;
  client = "";
  employee = "";
  employeeUsername = "";
  title = "";
  clientId: number;
  isActive: boolean = true;
  allComments: any[] = [];
  comments: any[] = [];
  visibleCount = 3;
  replyInputs: Record<number, string> = {};
  newComment: string = "";
  visibleReplyBox: number | null = null;
  loggedUsername: string = "";
  systemComment: CommentDTO | null = null;

  dataSource: CommentDTO[] = [];

  editingTitle = false;
  titleEdit = "";

  @ViewChild("anchor", { static: false }) anchor!: ElementRef;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private commentsService: CommentService,
    private staffingService: StaffingService,
    private refreshService: RefreshService,
    private auth: KeycloakAuthService,
    private location: Location
  ) {}

  ngOnInit(): void {
    this.loggedUsername = this.auth.getUsername();
    this.staffingId = +this.route.snapshot.params["id"];
    this.loadData(this.staffingId);

    this.refreshService.comments$.subscribe(() => {
      this.loadData(this.staffingId);
    });

    this.refreshService.staffing$.subscribe(() => {
      this.loadData(this.staffingId);
    });
  }

  loadData(staffingId: number): void {
    this.staffingService.getById(staffingId).subscribe((data) => {
      this.title = data.title;
      this.client = data.client.clientName;
      this.clientId = data.client.id;
      this.isActive = data.active;
      this.employee = `${data.employee.lastName} ${data.employee.firstName}`;
      this.employeeUsername = data.employee.username;
      this.comments = this.buildCommentTree(data.comments);
      // Look for the system-generated "completed" comment
      this.systemComment = data.comments.find(
        (c) =>
          c.title?.toLowerCase() === "process completed" &&
          c.comment
            ?.toLowerCase()
            .includes("has marked the process as completed")
      );
    });
  }

  ngAfterViewInit(): void {
    const interval = setInterval(() => {
      if (this.anchor?.nativeElement) {
        const observer = new IntersectionObserver((entries) => {
          if (entries[0].isIntersecting) {
            this.loadMore();
          }
        });

        observer.observe(this.anchor.nativeElement);
        clearInterval(interval);
      }
    }, 200);
  }

  loadMore() {
    const next = this.allComments.slice(
      this.comments.length,
      this.comments.length + this.visibleCount
    );
    this.comments.push(...next);
  }

  postReply(parent: any): void {
    const text = this.replyInputs[parent.id];

    if (text?.trim()) {
      const replyPayload = {
        title: "Reply",
        comment: text,
        commentParent: parent.id,
      };

      this.commentsService
        .addComment(this.staffingId, replyPayload)
        .subscribe(() => {
          this.replyInputs[parent.id] = "";
          this.visibleReplyBox = null;
          this.loadData(this.staffingId);
        });
    }
  }

  private buildCommentTree(comments: CommentDTO[]): CommentDTO[] {
    const commentMap = new Map<
      number,
      CommentDTO & { replies?: CommentDTO[] }
    >();
    const roots: CommentDTO[] = [];

    comments.forEach((comment) => {
      commentMap.set(comment.id, { ...comment, replies: [] });
    });

    comments.forEach((comment) => {
      if (comment.commentParent) {
        const parent = commentMap.get(comment.commentParent);
        parent?.replies?.push(commentMap.get(comment.id)!);
      } else {
        roots.push(commentMap.get(comment.id)!);
      }
    });

    return roots;
  }

  addComment(): void {
    if (this.newComment.trim()) {
      const payload = {
        title: "New Comment",
        comment: this.newComment,
        commentParent: null,
      };

      this.commentsService
        .addComment(this.staffingId, payload)
        .subscribe(() => {
          this.newComment = "";
          this.loadData(this.staffingId); // reload with new data
        });
    }
  }

  goBack() {
    this.location.back();
  }

  markAsCompleted(): void {
    this.staffingService.markAsCompleted(this.staffingId).subscribe(() => {
      this.router.navigate(["/staffing"]);
    });
  }

  isAdmin(): boolean {
    return this.auth.isAdmin();
  }

  openClient(id: number) {
    this.router.navigate(["/clients", id]);
  }

  openEmployeeProfile(username: string): void {
    this.router.navigate(["/users", username]);
  }

  saveTitle(): void {
    if (!this.titleEdit.trim()) return;

    this.staffingService
      .updateTitle(this.staffingId, this.titleEdit)
      .subscribe(() => {
        this.title = this.titleEdit;
        this.editingTitle = false;
      });
  }
  cancelTitleEdit(): void {
    this.titleEdit = this.title;
    this.editingTitle = false;
  }
}
