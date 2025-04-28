import { AbstractEntity } from './abstract-dto.model';
import { UserDTO } from './user-dto.model';

export interface CommentDTO extends AbstractEntity {
  title?: string;
  comment: string;
  commentParent?: number | null;
  author: UserDTO;
}
