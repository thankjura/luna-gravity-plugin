import { client } from "@/utils/client.ts";
import { ListenerScript, ListenerScriptsResponse, ListenerScriptWithProjects } from "@/interfaces/listener.ts";
import { DeleteResult } from "luna";

const ALLOWED_SCRIPT_KEYS: Array<keyof ListenerScript> = [
  'name',
  'description',
  'script',
  'enabled',
  'eventTypeIds',
  'projectIds',
  'async',
];

const filterAllowedFields = (data: Partial<ListenerScript>): Partial<ListenerScript> => {
  const filteredEntries = Object.entries(data).filter(([key]) =>
      ALLOWED_SCRIPT_KEYS.includes(key as keyof ListenerScript)
  );

  return Object.fromEntries(filteredEntries) as Partial<ListenerScript>;
};

class ListenerService {
  getAll() {
    return client.get<ListenerScriptsResponse>('/gravity/listener/scripts');
  }

  get(id: number) {
    return client.get<ListenerScriptWithProjects>(`/gravity/listener/scripts/${id}`);
  }

  patch(id: number, script: Partial<ListenerScript>) {
    script = filterAllowedFields(script);
    return client.patch<ListenerScriptWithProjects>(`/gravity/listener/scripts/${id}`, script);
  }

  delete(id: number) {
    return client.delete<DeleteResult>(`/gravity/listener/scripts/${id}`);
  }

  create(script: Partial<ListenerScript>) {
    script = filterAllowedFields(script);
    return client.post<ListenerScriptWithProjects>(`/gravity/listener/scripts`, script);
  }
}

export const listenerService = new ListenerService();