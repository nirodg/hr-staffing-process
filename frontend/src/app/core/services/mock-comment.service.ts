
import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';

export interface Comment {
  id: number;
  author: {
    id: number;
    name: string;
  };
  title: string;
  comment: string;
  commentParent: number | null;
  replies?: Comment[];
}

@Injectable({ providedIn: 'root' })
export class MockCommentService {
  private comments: Comment[] = [
    {
      id: 1,
      author: { id: 1, name: 'Maria Ionescu' },
      title: 'Initial Review',
      comment: 'Looks good, ready to move forward.',
      commentParent: null
    },
    {
      id: 2,
      author: { id: 2, name: 'Ion Popescu' },
      title: 'Re: Initial Review',
      comment: 'I agree, let’s proceed.',
      commentParent: 1
    },
    {
      id: 3,
      author: { id: 3, name: 'Elena Dobre' },
      title: 'Feedback',
      comment: 'Let’s validate with the client first.',
      commentParent: null
    },
    {
      id: 4,
      author: { id: 1, name: 'Maria Ionescu' },
      title: 'Follow-up',
      comment: 'Client gave approval.',
      commentParent: 3
    }
  ];

  getCommentsForProcess(processId: number): Observable<Comment[]> {
    const topLevel = this.comments.filter(c => c.commentParent === null);
    topLevel.forEach(parent => {
      parent.replies = this.comments.filter(c => c.commentParent === parent.id);
    });
    return of(topLevel);
  }
}
