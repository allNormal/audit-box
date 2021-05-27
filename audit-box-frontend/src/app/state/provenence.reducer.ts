import {Provenance} from '../entitiy/endpoint-entity';
import * as ProvenenceActions from '../state/provenence.actions'

export function provenenceReducer(state:Provenance[] = [], action: ProvenenceActions.Actions){
  switch (action.type) {
    case ProvenenceActions.ADD_PROVENENCE:
      return [...state, action.payload];
    case ProvenenceActions.REMOVE_PROVENENCE:
    default:
      return state;
  }
}
