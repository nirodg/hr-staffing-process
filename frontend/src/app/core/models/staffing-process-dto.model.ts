import { AbstractEntity } from './abstract-dto.model';
import { ClientDTO } from './client-dto.model';
import { CommentDTO } from './comment-dto.model';
import { UserDTO } from './user-dto.model';

export interface StaffingProcessDTO extends AbstractEntity {
  title?: string;
  client?: ClientDTO;
  employee?: UserDTO;
  comments?: CommentDTO[];
  active?: boolean;
}
