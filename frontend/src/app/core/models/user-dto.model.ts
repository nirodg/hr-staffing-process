import { AbstractEntity } from './abstract-dto.model';
import { RoleDTO } from './role-dto.model';

export interface UserDTO extends AbstractEntity {
  username?: string;
  firstName?: string;
  lastName?: string;
  position?: string;
  email?: string;
  available?: boolean;
  roles?: RoleDTO
}
